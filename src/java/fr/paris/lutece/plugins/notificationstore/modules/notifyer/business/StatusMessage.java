/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.paris.lutece.plugins.notificationstore.modules.notifyer.business;

/**
 *
 * @author seboo
 */
public class StatusMessage
{
    // Variables declarations
    private String _strType;
    private String _strStatus;
    private String _strReason;
    private String _strMessage;

    /**
     * Constructor
     * 
     * @param _strType
     * @param _strStatus
     * @param _strReason
     * @param _strMessage 
     */
    public StatusMessage(String _strType, String _strStatus, String _strReason, String _strMessage)
    {
        this._strType = _strType;
        this._strStatus = _strStatus;
        this._strReason = _strReason;
        this._strMessage = _strMessage;
    }
    
    

    /**
     * get type 
     * 
     * @return the type
     */
    public String getType( )
    {
        return _strType;
    }

    /**
     * set type
     * 
     * @param _strType 
     */
    public void setType(String _strType)
    {
        this._strType = _strType;
    }

    /**
     * get status 
     * 
     * @return the status 
     */
    public String getStatus( )
    {
        return _strStatus;
    }

    /**
     * set status
     * 
     * @param _strStatus 
     */
    public void setStatus(String _strStatus)
    {
        this._strStatus = _strStatus;
    }

    /**
     * get reason
     * 
     * @return the reason 
     */
    public String getReason( )
    {
        return _strReason;
    }

    /**
     * set reason
     * 
     * @param _strReason 
     */
    public void setReason(String _strReason)
    {
        this._strReason = _strReason;
    }

    /**
     * get message
     * 
     * @return the message 
     */
    public String getStrMessage( )
    {
        return _strMessage;
    }

    /**
     * set message
     * 
     * @param _strMessage 
     */
    public void setStrMessage(String _strMessage) 
    {
        this._strMessage = _strMessage;
    }
    
    /**
     * returns the property values of the statusMessage as a list of String
     * 
     * @return the list of attributes values as string list
     */
    public String asJson( )
    {
        return "{" +
                "\"type\":\""    + _strType    + "\"," +
                "\"status\":\""  + _strStatus  + "\"," +
                "\"reason\":\""  + (_strReason == null ? "" : _strReason.replace( "\"","\\\"").replace("\n", "\\\\n").replace("\r", "\\\\r").replace("\t", "\\\\t")) + "\"," +
                "\"message\":\"" + (_strMessage == null ? "": _strMessage.replace( "\"","\\\"").replace("\n", "\\\\n").replace("\r", "\\\\r").replace("\t", "\\\\t")) + "\"}";
    }

    
}
