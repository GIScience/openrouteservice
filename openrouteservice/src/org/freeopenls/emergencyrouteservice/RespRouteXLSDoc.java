/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.emergencyrouteservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.opengis.xls.XLSDocument;


import org.apache.xmlbeans.XmlOptions;
import org.freeopenls.emergencyrouteservice.ERSConstants;



/**
 * <p><b>Title: Class for Routing </b></p>
 * <p><b>Description:</b> Class for Routing </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-06-05
 */
public class RespRouteXLSDoc{
	/** XLSDocument */
	private XLSDocument m_xlsDocOut;

    /**
     * Constructor
     * 
     * @param xlsDoc
     * 			XLSDocument
     */
    public RespRouteXLSDoc(XLSDocument xlsDoc) {
    	m_xlsDocOut = xlsDoc;
    }

    /**
     * Method that returns length of the content in bytes.
     * 
     * @return int 
     * 			Returns the length of the content in bytes.
     * @throws IOException 
     * 			If the transformation of the XLSDocument into a byte[] failed
     */
    public int getContentLength() throws IOException {
        return getByteArray().length;
    }

    /**
     * Method that returns content type of this response
     * 
     * @return String
     * 			Returns the value of the constant RSConstants.CONTENT_TYPE_XML.
     */
    public String getContentType() {
        return ERSConstants.CONTENT_TYPE_XML;
    }

    /**
     * Method that returns response as byte[].
     * 
     * @return byte[]
     * 			Returns the response as byte[]
     * @throws IOException
     * 			If the transformation of the XLSDocument into a byte[] failed
     */
    public byte[] getByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlOptions options = new XmlOptions();
        options.setSaveNamespacesFirst();
        options.setSaveAggressiveNamespaces();
        options.setSavePrettyPrint();
        m_xlsDocOut.save(baos,options);
        byte[] bytes = baos.toByteArray();
        
        return bytes;
    }
}