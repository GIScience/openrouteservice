
package org.freeopenls.wfs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.SeverityType;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

import org.freeopenls.error.ServiceError;


/**
 * <p><b>Title: GetFeature</b></p>
 * <p><b>Description:</b>Class for GetFeature WFS.<br>
 * Create the GetFeature-Request(WFS) and return the results.<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-06-06
 */
public class GetFeature {
	/** Logger, used to log errors(exceptions) and additonaly information */
	private static final Logger mLogger = Logger.getLogger(GetFeature.class.getName());
	/** WFS Path */
	private String m_sWFSPath = "";

	/**
	 * Constructor
	 */
	public GetFeature(String sWFSPath){
        m_sWFSPath = sWFSPath;
	}

	/**
	 * 
	 * @param sWFS_LayerName
	 * @param dWithinDistance
	 * @param linestring
	 * @return FeatureCollection
	 * @throws ServiceError
	 */
	public FeatureCollection getFeatureDWithin(String sWFS_LayerName, double dWithinDistance, LineString linestring)throws ServiceError{
		FeatureSchema featsch = new FeatureSchema();
		featsch.addAttribute("id", AttributeType.STRING);
		featsch.addAttribute("name", AttributeType.STRING);
		featsch.addAttribute("use", AttributeType.STRING);
		featsch.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
		FeatureCollection landmarkcoll = new FeatureDataset(featsch);
		
		try{
			///////////////////////////////////////
			//*** Create Request to WFS ***
			String sCoords = "";
			Coordinate c[] = linestring.getCoordinates();
			for(int i=0 ; i<c.length ; i++){
				sCoords = sCoords + " " +c[i].x+ "," +c[i].y;
			}
	
			String sRequest = "" +
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
					+"	<wfs:GetFeature service=\"WFS\" version=\"1.0.0\" \n"
					+" 		outputFormat=\"GML2\" \n"
					+" 		xmlns:topp=\"http://www.openplans.org/topp\" \n"
					+" 		xmlns:wfs=\"http://www.opengis.net/wfs\" \n"
					+" 		xmlns:ogc=\"http://www.opengis.net/ogc\" \n"
					+" 		xmlns:gml=\"http://www.opengis.net/gml\" \n"
					+" 		xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
					+" 		xsi:schemaLocation=\"http://www.opengis.net/wfs \n"
					+"         http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd\"> \n"
					+"	<wfs:Query typeName=\""+sWFS_LayerName+"\"> \n"
					+"	    <ogc:Filter> \n"
					+"			<ogc:DWithin> \n"
					+"				<ogc:PropertyName>the_geom</ogc:PropertyName> \n"
					+"				<gml:LineString> \n"
					+"					<gml:coordinates>"+sCoords+"</gml:coordinates> \n"
					+"				</gml:LineString> \n"
					+"			    <Distance units='http://www.uomdict.com/uom.html#meters'>"+dWithinDistance+"</Distance> \n"
					+"			</ogc:DWithin> \n"
					+"	    </ogc:Filter> \n"
					+"	  </wfs:Query> \n"
					+"	</wfs:GetFeature>";
			
			// Send data
			URL u = new URL(m_sWFSPath);
	        HttpURLConnection acon = (HttpURLConnection) u.openConnection();
	        acon.setAllowUserInteraction(false);
	        acon.setRequestMethod("POST");
	        acon.setRequestProperty("Content-Type", "application/xml");
	        acon.setDoOutput(true);
	        acon.setDoInput(true);
	        acon.setUseCaches(false);
	        PrintWriter xmlOut = null;
	        xmlOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(acon.getOutputStream())));
	        xmlOut = new java.io.PrintWriter(acon.getOutputStream());
//log.info("Request to WFS: "+sRequest.toString());
	        xmlOut.write(sRequest);
	        xmlOut.flush();
	        xmlOut.close();
	
			// Get the response
	        InputStream is = acon.getInputStream();
	        
			//Read XML InsputStream
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(is);
//log.info("Response WFS: "+doc.toString());
			is.close();

			//RootElement
			Element elementRoot = doc.getRootElement();
			List listMainElements = elementRoot.getChildren();
			
			//TMP Objects
			String sFeatID_TMP = "";
			String sName = "";
			String sUse = "";
			Polygon poly = null;
			
			for (int j = 0; j < listMainElements.size(); j++) {
				Element elementMain = (Element) (listMainElements.get(j));

				//Main Element
				if (elementMain.getName().equals("featureMember")) {
					List listCildren = elementMain.getChildren();

					for (int k = 0; k < listCildren.size(); k++) {
						Element elementMember = (Element) (listCildren.get(k));
						sFeatID_TMP = elementMember.getAttributeValue("fid").trim();
						
						if(!sFeatID_TMP.equals("")){
						
	    					List listLayerChildren = elementMember.getChildren();
	    					
	    					//Element geom = elementMember.getChild("outerBoundaryIs");

	    					for(int m=0 ; m<listLayerChildren.size() ; m++){
	    						Element elementsLayer = (Element) listLayerChildren.get(m);
	    						
	    						//*read geometry
	    						if(elementsLayer.getName().equals("the_geom")){
	    							List listElementsLayerChildren = elementsLayer.getChildren();
	    							
	    	    					for(int n=0 ; n<listElementsLayerChildren.size() ; n++){
	    	    						Element elementN = (Element) listElementsLayerChildren.get(n);
	    	    						List ListN = elementN.getChildren();

		    	    					for(int o=0 ; o<ListN.size() ; o++){
		    	    						Element elementO = (Element) ListN.get(o);
		    	    						List ListO = elementO.getChildren();
			    	    					
		    	    						//Element: MultiLineString, Polygon, LineString
		    	    						for(int p=0 ; p<ListO.size() ; p++){
			    	    						Element elementP = (Element) ListO.get(p);
			    	    						List ListP = elementP.getChildren();

			    	    						//Element: outerBoundaryIs
			    	    						for(int q=0 ; q<ListP.size() ; q++){
				    	    						Element elementQ = (Element) ListP.get(q);
				    	    						ArrayList<Coordinate> alCoordTMP = new ArrayList<Coordinate>();
					    							String sTMP = elementQ.getValue().trim();
					    							
					    							boolean boolcoords = true;
					    							while(boolcoords){
					    								if(sTMP.indexOf(" ")>0){
					    									Coordinate cTMP = getCoordinate(sTMP.substring(0 , sTMP.indexOf(" ")));
					    									sTMP = sTMP.substring(sTMP.indexOf(" ")).trim();
					    									alCoordTMP.add(cTMP);
					    								}
					    								else if(sTMP.indexOf(",") == 1){
					    									Coordinate cTMP = getCoordinate(sTMP);
					    									alCoordTMP.add(cTMP);
					    									boolcoords = false;
					    								}
					    								else{
					    									boolcoords = false;
					    								}	
					    							}
					    							
					    							Coordinate cTMP[] = new Coordinate[alCoordTMP.size()+1];
					    							for(int i=0 ; i<alCoordTMP.size() ; i++){
					    								cTMP[i] = alCoordTMP.get(i);
					    							}
					    							cTMP[alCoordTMP.size()] = cTMP[0];
					    							
					    							GeometryFactory gf = new GeometryFactory();
					    							//LineString ls = gf.createLineString(cTMP);
					    							LinearRing lr = gf.createLinearRing(cTMP);
					    							poly = gf.createPolygon(lr, null);
				    	    					}
			    	    					}
		    	    					}
	    	    					}
	    						}
	    						//Get ID
	    						if(elementsLayer.getName().equals("id")){
	    							//log.info(elementsLayer.getText());
	    						}
	    						//Get Name
	    						if(elementsLayer.getName().equalsIgnoreCase("name")){
	    							sName = elementsLayer.getTextTrim();
	    							//log.info(elementsLayer.getText());
	    						}
	    						//Get Nutzung
	    						if(elementsLayer.getName().equalsIgnoreCase("nutzung")){
	    							sUse = elementsLayer.getTextTrim();
	    							//log.info(elementsLayer.getText());
	    						}
	    						// GET ???
	    						//log.info(elementsLayer.getName());

	    						//Add the landmark to collection
    							BasicFeature feat = new BasicFeature(featsch);
    							feat.setAttribute("id", sFeatID_TMP);
    							feat.setAttribute("name", sName);
    							feat.setAttribute("use", sUse);
    							// more attributes ..?
    							feat.setGeometry(poly.getCentroid());
    							
    							landmarkcoll.add(feat);
	    					}
						}
						else{
							sFeatID_TMP = "";
							sName = "";
							sUse = "";
	    				}	
					}
				}
			}
		}
		catch (JDOMException jdome) {
			mLogger.error(jdome);
			ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addInternalError(ErrorCodeType.UNKNOWN, "RSwL", jdome);
            throw se;
		}
		catch (IOException ioe) {
			mLogger.error(ioe);
			ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addInternalError(ErrorCodeType.UNKNOWN, "RSwL", ioe);
            throw se;
		}

		return landmarkcoll;
	}

	private Coordinate getCoordinate(String sStringValue)throws ServiceError{
		Coordinate cTMP;
		
		if(sStringValue != null && sStringValue.indexOf(",") >= 0){
			cTMP = new Coordinate(Double.valueOf(sStringValue.substring(0, sStringValue.indexOf(","))) , Double.valueOf(sStringValue.substring(sStringValue.indexOf(",")+1, sStringValue.length())));
		}else{
			ServiceError se = new org.freeopenls.error.ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED,
					"xls:Position / gml:Point / gml:pos",
					"The required value of the mandatory parameter 'gml:pos' is missing or not right"
					+ "'. Delivered value was: '"+sStringValue+"'");
			throw se;
		}
		return cTMP;
	}

//////////////////////////////////////
//*** test environment ***
/*
	public static void main(String[] args) {
		Coordinate c[] = new Coordinate[2];
		c[0] = new Coordinate(3432985,5793898);
		c[1] = new Coordinate(3433182,5793832);
		
		GeometryFactory gf = new GeometryFactory();
		LineString ls[] = new LineString[1];
		ls[0] =  gf.createLineString(c);
		MultiLineString mlst = gf.createMultiLineString(ls);
		
		try{
			GetFeatureDWithin getfeat = new GetFeatureDWithin("http://localhost:8080/geoserver/wfs", "osna_alk:Osna_ALK_NebenGeb", 30, mlst);
			
			ArrayList<String> test = getfeat.getFeatIDs();
			for(int i=0 ; i<test.size() ; i++){
				System.out.println(test.get(i));
			}
			if(test.size() == 0)
				System.out.println("That should not happend!!");

		}
		catch(ServiceError se){
			System.out.println(se);
		}
	}
*/
//////////////////////////////////////
}
