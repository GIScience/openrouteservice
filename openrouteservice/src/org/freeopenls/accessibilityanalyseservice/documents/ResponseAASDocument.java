/****************************************************
 Copyright (C) 2006-2007 by Pascal Neis

 Author: Pascal Neis

 Contact: Pascal Neis, Herm-Schuster-Str. 57,
 65510 Hünstetten, Germany, pascal.neis@gmail.com
*****************************************************/

package org.freeopenls.accessibilityanalyseservice.documents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.xmlbeans.XmlOptions;

import de.fhMainz.geoinform.aas.AASDocument;

/**
 * Class RespAASDoc - For response of a request to AAS.
 * 
 * @author Pascal Neis, pascal.neis@geoinform.fh-mainz.de
 * @version 1.0 2007-01-05
 */
public class ResponseAASDocument{
	/** AASDocument */
	private AASDocument aasDocOut;

    /**
     * Constructor
     * 
     * @param aasDoc
     * 			AASDocument
     */
    public ResponseAASDocument(AASDocument aasDoc) {
    	this.aasDocOut = aasDoc;
    }

    /**
     * Method that returns length of the content in bytes.
     * 
     * @return int 
     * 			- Returns the length of the content in bytes.
     * @throws	IOException 
     * 			If the transformation of the AASDocument into a byte[] failed
     */
    public int getContentLength() throws IOException {
        return getByteArray().length;
    }

    /**
     * Method that returns content type of this response
     * 
     * @return String
     * 			- Returns the value of the constant AccessibilityConfigurator.CONTENT_TYPE_XML.
     */
    public String getContentType() {
        return "text/xml";
    }

    /**
     * Method that returns response as byte[].
     * 
     * @return byte[]
     * 			- Returns the response as byte[].
     * @throws IOException
     *			If the transformation of the ASDocument into a byte[] failed
     */
    public byte[] getByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlOptions options = new XmlOptions();
        options.setSaveNamespacesFirst();
        options.setSaveAggressiveNamespaces();
        options.setSavePrettyPrint();
        this.aasDocOut.save(baos,options);
        byte[] bytes = baos.toByteArray();
        
        return bytes;
    }
}