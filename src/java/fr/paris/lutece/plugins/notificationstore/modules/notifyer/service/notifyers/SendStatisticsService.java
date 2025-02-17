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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.plugins.crmclient.util.CRMException;
import fr.paris.lutece.plugins.grubusiness.business.notification.Notification;
import fr.paris.lutece.plugins.grubusiness.service.notification.INotifyerServiceProvider;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccess;

public class SendStatisticsService implements INotifyerServiceProvider
{
	private static final String URL_WS_CREATE_DEMAND = "notificationstore.url.ws.statistics";

	/** constructor */
	public SendStatisticsService()
	{
	}

	/**
	 * Send Notification to statistics
	 * 
	 * @param n
	 * @throws Exception
	 */
	public void process( Notification notification )
	{
		AppLogService.info( " \n \n GRU NOTIFYER - sendNotification( NotificationDTO notification ) \n \n" );

		doProcess( AppPropertiesService.getProperty( URL_WS_CREATE_DEMAND ), notification );
	}

	/**
	 * Call web service rest using GET method
	 * 
	 * @param strWsUrl
	 *            the web service URL
	 * 
	 * @param notification
	 *            the parameters
	 * @return the response
	 * @throws Exception
	 * @throws CRMException
	 */
	private Response doProcess( String strWsUrl, Notification notification )
	{
		ObjectMapper mapper = new ObjectMapper();
		HttpAccess clientHttp = new HttpAccess();
		Map<String, String> mapHeadersResponse = new HashMap<String, String>();
		Map<String, String> mapHeadersRequest = new HashMap<String, String>();
		mapHeadersRequest.put( HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON );

		Response oResponse = null;

		try
		{
			String strJSON = mapper.writeValueAsString( notification );
			String strResponseJSON = clientHttp.doPostJSON( strWsUrl, strJSON, mapHeadersRequest, mapHeadersResponse );
			oResponse = mapper.readValue( strResponseJSON, Response.class );
		}
		catch ( Exception e )
		{
			String strError = "Error connecting to '" + strWsUrl + "' : ";
			AppLogService.error( strError + e.getMessage(), e );
		}

		return oResponse;
	}
        
        @Override
        public String getName() {
            return this.getClass( ).getName( );
        }
}
