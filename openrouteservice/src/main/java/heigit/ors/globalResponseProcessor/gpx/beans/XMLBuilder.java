/*
 *
 *  *
 *  *  *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *  *  *
 *  *  *   http://www.giscience.uni-hd.de
 *  *  *   http://www.heigit.org
 *  *  *
 *  *  *  under one or more contributor license agreements. See the NOTICE file
 *  *  *  distributed with this work for additional information regarding copyright
 *  *  *  ownership. The GIScience licenses this file to you under the Apache License,
 *  *  *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  *  *  with the License. You may obtain a copy of the License at
 *  *  *
 *  *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  *  Unless required by applicable law or agreed to in writing, software
 *  *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  *  See the License for the specific language governing permissions and
 *  *  *  limitations under the License.
 *  *
 *
 */

package heigit.ors.globalResponseProcessor.gpx.beans;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * This classs generates the xml representation of the gpx file as a formatted string.
 * The JAXB Marshaller goes recursively through all the classes.
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class XMLBuilder {

    /**
     * {@link XMLBuilder} functions as an empty placeholder class.
     */
    public XMLBuilder() {

    }

    /**
     * The function creates a XML Element from a GPX and returns it as a string representation.
     *
     * @param gpx Needs a gpx as an Input.
     * @return Returns the GPX as a well formatted XML
     * @throws JAXBException Throws {@link JAXBException} exception in case of failure
     */

    public String build(Gpx gpx) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Gpx.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        m.marshal(gpx, sw);
        return sw.toString();
    }
}
