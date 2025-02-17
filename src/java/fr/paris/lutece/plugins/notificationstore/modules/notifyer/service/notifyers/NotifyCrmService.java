/*
 * Copyright (c) 2002-2015, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.notificationstore.modules.notifyer.service.notifyers;

import java.util.HashMap;
import java.util.Map;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import fr.paris.lutece.plugins.crmclient.business.CRMItemTypeEnum;
import fr.paris.lutece.plugins.crmclient.business.ICRMItem;
import fr.paris.lutece.plugins.crmclient.util.CRMException;
import fr.paris.lutece.plugins.grubusiness.business.notification.Notification;
import fr.paris.lutece.plugins.grubusiness.service.notification.INotifyerServiceProvider;
import fr.paris.lutece.plugins.grubusiness.service.notification.NotificationException;
import fr.paris.lutece.plugins.notificationstore.utils.NotificationStoreConstants;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

public class NotifyCrmService implements INotifyerServiceProvider
{
    private static final String CRM_REMOTE_ID = "remote_id";
    private static final String KEY_DEMAND = "demand";
    private static final String SLASH = "/";
    private static final String URL_WS_GET_DEMAND = "notificationstore.url.ws.getDemand";
    private static final String URL_WS_CREATE_DEMAND = "notificationstore.url.ws.createDemandByUserGuid";
    private static final String URL_WS_UPDATE_DEMAND = "notificationstore.url.ws.updateDemand";
    private static final String URL_WS_NOTIFY_DEMAND = "notificationstore.url.ws.notifyDemand";
    
    private static JsonMapper _mapper = new JsonMapper();

    /** constructor */
    public NotifyCrmService( )
    {
    }

    @Override
    public String getName() {
        return this.getClass( ).getName( );
    }
    
    /**
     * Tests whether the demand already exists or not
     * 
     * @param notif
     *            the notification
     * @return {@code true} if the demand already exists, {@code false} otherwise
     * @throws CRMException
     *             if there is an exception during the treatment
     */
    public boolean isExistDemand( Notification notif ) throws CRMException
    {
        
        AppLogService.info( " \n \n GRU NOTIFIER - isExistDemand( NotificationDTO notif ) \n \n" );

        String strIdDemandType = notif.getDemand( ).getTypeId( );
        String strIdRemoteDemand = String.valueOf( notif.getDemand( ).getId( ) );

        String strResponse = doProcess( AppPropertiesService.getProperty( URL_WS_GET_DEMAND ) + SLASH + strIdDemandType + SLASH + strIdRemoteDemand );

        try 
        {
        	JsonNode node = _mapper.readTree( strResponse );
        	if ( node.has( KEY_DEMAND ) ) 
        	{
        		return true;
        	}
        }
        catch ( JsonProcessingException  e )
        {
        	return false;
        }
        
        return false;
    }

    /**
     * Create CRM Demand
     * 
     * @param notif
     * @throws CRMException
     */
    public void createDemand( Notification notif ) throws CRMException
    {
        AppLogService.info( " \n \n GRU NOTIFIER - sendCreateDemand( NotificationDTO notif ) \n \n" );

        ICRMItem crmItem = buildCrmItemForDemand( notif, CRMItemTypeEnum.DEMAND_CREATE_BY_USER_GUID );

        doProcess( crmItem, AppPropertiesService.getProperty( URL_WS_CREATE_DEMAND ) );
    }

    /**
     * Updates a CRM Demand
     * 
     * @param notif
     *            the notification
     * @throws CRMException
     *             if there is an exception during the treatment
     */
    public void updateDemand( Notification notif ) throws CRMException
    {
        AppLogService.info( " \n \n GRU NOTIFIER - updateDemand( NotificationDTO notif ) \n \n" );

        ICRMItem crmItem = buildCrmItemForDemand( notif, CRMItemTypeEnum.DEMAND_UPDATE );

        doProcess( crmItem, AppPropertiesService.getProperty( URL_WS_UPDATE_DEMAND ) );
    }


    @Override
    public void process( Notification notification ) throws NotificationException
    {
        

        if ( notification != null &&  notification.getMyDashboardNotification( ) != null )
        {
            
            try
            {            
                if ( !isExistDemand( notification ) )
                {
                    createDemand( notification );
                }
                else
                {
                    updateDemand( notification );
                }

                ICRMItem crmItem = buildCrmItemForNotification( notification, CRMItemTypeEnum.NOTIFICATION );

                doProcess( crmItem, AppPropertiesService.getProperty( URL_WS_NOTIFY_DEMAND ) );
            }
            catch( CRMException ex )
            {
                throw new NotificationException( ex.getMessage( ), ex ) ;
            }
        }
    }

    /**
     * Call web service rest using POST method
     * 
     * @param crmItem
     *            the parameters
     * @param strWsUrl
     *            the web service URL
     * @return the response
     * @throws CRMException
     */
    private String doProcess( ICRMItem crmItem, String strWsUrl ) throws CRMException
    {
        String strResponse = StringUtils.EMPTY;

        try
        {
            HttpAccess httpAccess = new HttpAccess( );
            strResponse = httpAccess.doPost( strWsUrl, crmItem.getParameters( ) );
        }
        catch( HttpAccessException e )
        {
            String strError = "Error connecting to '" + strWsUrl + "' : ";
            AppLogService.error( strError + e.getMessage( ), e );
            throw new CRMException( strError, e );
        }

        return strResponse;
    }

    /**
     * Call web service rest using GET method
     * 
     * @param crmItem
     *            the parameters
     * @param strWsUrl
     *            the web service URL
     * @return the response
     * @throws CRMException
     */
    private String doProcess( String strWsUrl ) throws CRMException
    {
        String strResponse = StringUtils.EMPTY;

        try
        {
            HttpAccess httpAccess = new HttpAccess( );
            Map<String, String> mapHeaders = new HashMap<String, String>( );
            mapHeaders.put( HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString() );

            strResponse = httpAccess.doGet( strWsUrl, null, null, mapHeaders );
        }
        catch( HttpAccessException e )
        {
            String strError = "Error connecting to '" + strWsUrl + "' : ";
            AppLogService.error( strError + e.getMessage( ), e );
            throw new CRMException( strError, e );
        }

        return strResponse;
    }

    /**
     * Builds a CrmItem object for a demand
     * 
     * @param notif
     *            the notification
     * @param crmItemType
     *            the CrmItemType
     * @return the CrmItem
     */
    private static ICRMItem buildCrmItemForDemand( Notification notif, CRMItemTypeEnum crmItemType )
    {
        ICRMItem crmItem = SpringContextService.getBean( crmItemType.toString( ) );

        crmItem.putParameter( ICRMItem.ID_DEMAND_TYPE,
                ( ( notif.getDemand( ) != null ) && ( StringUtils.isNotBlank( notif.getDemand( ).getTypeId( ) ) ) ) ? notif.getDemand( ).getTypeId( )
                        : StringUtils.EMPTY );

        crmItem.putParameter(
                ICRMItem.USER_GUID,
                ( ( notif.getDemand( ) != null ) && ( notif.getDemand( ).getCustomer( ) != null ) && ( StringUtils.isNotBlank( notif.getDemand( ).getCustomer( )
                        .getConnectionId( ) ) ) ) ? notif.getDemand( ).getCustomer( ).getConnectionId( ) : StringUtils.EMPTY );

        crmItem.putParameter(
                ICRMItem.ID_STATUS_CRM,
                ( ( notif.getMyDashboardNotification( ) != null ) 
                		&& ( notif.getMyDashboardNotification( ).getStatusId( ) != NotificationStoreConstants.DEFAULT_INT ) ) ? String
                        .valueOf( notif.getMyDashboardNotification( ).getStatusId( ) ) : StringUtils.EMPTY );

        crmItem.putParameter( ICRMItem.STATUS_TEXT, ( ( notif.getMyDashboardNotification( ) != null ) && ( StringUtils.isNotBlank( notif
                .getMyDashboardNotification( ).getStatusText( ) ) ) ) ? notif.getMyDashboardNotification( ).getStatusText( ) : StringUtils.EMPTY );

        crmItem.putParameter( ICRMItem.DEMAND_DATA, ( ( notif.getMyDashboardNotification( ) != null ) && ( StringUtils.isNotBlank( notif
                .getMyDashboardNotification( ).getData( ) ) ) ) ? notif.getMyDashboardNotification( ).getData( ) : StringUtils.EMPTY );

        crmItem.putParameter( CRM_REMOTE_ID, ( ( notif.getDemand( ) != null ) && ( StringUtils.isNotBlank( notif.getDemand( ).getId( ) ) ) ) ? notif
                .getDemand( ).getId( ) : StringUtils.EMPTY );

        return crmItem;
    }

    /**
     * Builds a CrmItem object for a notification
     * 
     * @param notif
     *            the notification
     * @param crmItemType
     *            the CrmItemType
     * @return the CrmItem
     */
    private static ICRMItem buildCrmItemForNotification( Notification notif, CRMItemTypeEnum crmItemType )
    {
        ICRMItem crmItem = SpringContextService.getBean( crmItemType.toString( ) );

        crmItem.putParameter( CRM_REMOTE_ID, ( ( notif.getDemand( ) != null ) && ( StringUtils.isNotBlank( notif.getDemand( ).getId( ) ) ) ) ? notif
                .getDemand( ).getId( ) : StringUtils.EMPTY );

        crmItem.putParameter( ICRMItem.ID_DEMAND_TYPE,
                ( ( notif.getDemand( ) != null ) && ( StringUtils.isNotBlank( notif.getDemand( ).getTypeId( ) ) ) ) ? notif.getDemand( ).getTypeId( )
                        : StringUtils.EMPTY );

        crmItem.putParameter( ICRMItem.NOTIFICATION_OBJECT, ( ( notif.getMyDashboardNotification( ) != null ) && ( StringUtils.isNotBlank( notif
                .getMyDashboardNotification( ).getSubject( ) ) ) ) ? notif.getMyDashboardNotification( ).getSubject( ) : StringUtils.EMPTY );

        crmItem.putParameter( ICRMItem.NOTIFICATION_MESSAGE, ( ( notif.getMyDashboardNotification( ) != null ) && ( StringUtils.isNotBlank( notif
                .getMyDashboardNotification( ).getMessage( ) ) ) ) ? notif.getMyDashboardNotification( ).getMessage( ) : StringUtils.EMPTY );

        crmItem.putParameter( ICRMItem.NOTIFICATION_SENDER, ( ( notif.getMyDashboardNotification( ) != null ) && ( StringUtils.isNotBlank( notif
                .getMyDashboardNotification( ).getSenderName( ) ) ) ) ? notif.getMyDashboardNotification( ).getSenderName( ) : StringUtils.EMPTY );

        return crmItem;
    }

}
