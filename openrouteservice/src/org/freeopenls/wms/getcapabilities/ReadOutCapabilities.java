

package org.freeopenls.wms.getcapabilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.helpers.DefaultHandler;
import org.jdom.Document;
import org.jdom.Element;

/**
 * <p><b>Title: ReadOutCapabilities</b></p>
 * <p><b>Description:</b> Class for ReadOutCapabilities<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-12-10
 */
public class ReadOutCapabilities extends DefaultHandler {
	/** Informations about the WMS **/
	private WMS m_WMSInfos = new WMS();
	/** ArrayList with all Layers **/
	private ArrayList<LayerInfo> m_alLayers = new ArrayList<LayerInfo>();
	/** GetCapabilities Document of the WMS */
	private static Document m_document;

	/**
	 * Constructor
	 * 
	 * @param doc XML Dokument
	 * @throws IOException
	 */
	public ReadOutCapabilities(Document doc) throws IOException {

		///////////////////////////////////////
		//XML Document
		m_document = doc;
		Element elementroot = m_document.getRootElement();
		List listMainElements = elementroot.getChildren();
		int anz = listMainElements.size();

		///////////////////////////////////////
		//*** Read Out XML Response ***
		for(int i=0 ; i<anz ; i++) {
			Element elementMain = (Element) listMainElements.get(i);

			//**** Read Out Service Element ****
			if (elementMain.getName().equals("Service")) {
				//System.out.println("SERVICE Element");
				List listService = elementMain.getChildren();

				for (int k = 0; k < listService.size(); k++) {
					Element elementService = (Element) (listService.get(k));
					//System.out.println(elementService.getName()+": "+elementService.getTextTrim());
					if(elementService.getName().equalsIgnoreCase("Name"))
						m_WMSInfos.Name = elementService.getTextTrim();
					if(elementService.getName().equalsIgnoreCase("Title"))
						m_WMSInfos.Title = elementService.getTextTrim();
					if(elementService.getName().equalsIgnoreCase("Abstract"))
						m_WMSInfos.Abstract = elementService.getTextTrim();
				}
			}
			
			//**** Read Out Capability Element ****
			if (elementMain.getName().equals("Capability")) {
				//System.out.println("CAPABILITY Element");
				List listCapability = elementMain.getChildren();

				for (int k = 0; k < listCapability.size(); k++) {
					Element elementCapability = (Element) (listCapability.get(k));
					//System.out.println(elementCapability.getName()+" : "+elementCapability.getTextTrim());
					
					//*** Read Out Request Element ***
					if(elementCapability.getName().equalsIgnoreCase("Request")){
						List listRequest = elementCapability.getChildren();
						for (int l = 0; l < listRequest.size(); l++) {
							Element elementRequest = (Element) (listRequest.get(l));
							//** Read Out GetMap Element **
							if(elementRequest.getName().equalsIgnoreCase("GetMap")){
								List listGetMap = elementRequest.getChildren();
								for (int m = 0; m < listGetMap.size(); m++) {
									Element elementGetMap = (Element) (listGetMap.get(m));
									//System.out.println(elementGetMap.getName()+": "+elementGetMap.getTextTrim());
									if(elementGetMap.getName().equalsIgnoreCase("Format")){
										m_WMSInfos.Format.add(elementGetMap.getTextTrim());
									}
								}
							}
						}
					}
					
					//*** Read Out Layer Element ***
					if(elementCapability.getName().equalsIgnoreCase("Layer")){
						List listLayer = elementCapability.getChildren();

						for (int l = 0; l < listLayer.size(); l++) {
							Element elementLayer = (Element) (listLayer.get(l));
							//System.out.println(elementLayer.getName()+" : "+elementLayer.getTextTrim());
							
							//** Read Out SRS Element **
							if(elementLayer.getName().equalsIgnoreCase("SRS")){
								m_WMSInfos.SRS.add(elementLayer.getTextTrim());
							}
							//** Read Out Layer Element **
							if(elementLayer.getName().equalsIgnoreCase("Layer")){
								if(elementLayer.getAttribute("queryable").getValue().equalsIgnoreCase("1")){
									LayerInfo layerinfoTMP = new LayerInfo();
									List listqueryableLayer = elementLayer.getChildren();
									for (int m=0; m<listqueryableLayer.size(); m++) {
										Element elementqueryableLayer = (Element) (listqueryableLayer.get(m));
										//System.out.println(elementqueryableLayer.getName()+" : "+elementqueryableLayer.getTextTrim());
										//Name
										if(elementqueryableLayer.getName().equalsIgnoreCase("Name")){
											layerinfoTMP.Name = elementqueryableLayer.getTextTrim();
										}
										//Style
										if(elementqueryableLayer.getName().equalsIgnoreCase("Style")){
											List listqueryableLayerStlye = elementqueryableLayer.getChildren();
											for (int n=0; n<listqueryableLayerStlye.size() ; n++) {
												Element elementqueryableLayerStyle = (Element) (listqueryableLayerStlye.get(n));
												//System.out.println(elementqueryableLayerStyle.getName()+" : "+elementqueryableLayerStyle.getTextTrim());
												//Name
												if(elementqueryableLayerStyle.getName().equalsIgnoreCase("Name")){
													layerinfoTMP.Style = elementqueryableLayerStyle.getTextTrim();
												}
											}
										}
									}
									m_alLayers.add(layerinfoTMP);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Method that return a arraylist with all layers.
	 * @return ArrayList with all Layers
	 */
	public ArrayList<LayerInfo> getLayerList() {
		return m_alLayers;
	}

	/**
	 * Method that returns ...
	 * @return WMS
	 */
	public WMS getWMSInfos() {
		return m_WMSInfos;
	}


//------------------------------------------------------------------------
	///////////////////////////////////////
	//*** Class WMS ***
	public class WMS {
		public String Name;
		public String Title;
		public String Abstract;
		public String ContactPerson;
		public String ContactOrganisation;
		public String ContactPosition;
		public String ContactVoiceTelephone;
		public String ContactElectronicMail;
		public String sServerURL;
		public ArrayList<String> Format = new ArrayList<String>();
		public ArrayList<String> SRS = new ArrayList<String>();

		public WMS() {
			Name = "";
			Title = "";
			Abstract = "";
			ContactPerson = "";
			ContactOrganisation = "";
			ContactPosition = "";
			ContactVoiceTelephone = "";
			ContactElectronicMail = "";
			sServerURL = "";
		}
	}
//------------------------------------------------------------------------
	///////////////////////////////////////
	//*** Class BBox ***
	public class BBox {
		String EPSG;
		double minX;
		double minY;
		double maxX;
		double maxY;

		public BBox() {
			EPSG = "";
			maxX = 0.0;
			maxY = 0.0;
			minX = 0.0;
			minY = 0.0;
		}
		public String getEPSG() {return EPSG;}
		public void setEPSG(String epsg) {EPSG = epsg;}
		public double getMaxX() {return maxX;}
		public void setMaxX(double maxX) {this.maxX = maxX;}
		public double getMaxY() {return maxY;}
		public void setMaxY(double maxY) {this.maxY = maxY;}
		public double getMinX() {return minX;}
		public void setMinX(double minX) {this.minX = minX;}
		public double getMinY() {return minY;}
		public void setMinY(double minY) {this.minY = minY;};
	}
//------------------------------------------------------------------------
	///////////////////////////////////////
	//*** Class LayerInfo ***
	public class LayerInfo {
		String Name;
		String Titel;
		String EPSG;
		BBox bbox;
		String Style;
		double TileMinSize;
		double tileMaxSize;
		boolean splitTilesOnHighestLOD;
		
		public LayerInfo() {
			Name = "";
			Titel = "";
			EPSG = "";
			Style = "";
			bbox = new BBox();
		}
		public BBox getBbox() {return bbox;}
		public void setBbox(BBox bbox) {this.bbox = bbox;}
		public String getEPSG() {return EPSG;}
		public void setEPSG(String epsg) {EPSG = epsg;}
		public String getName() {return Name;}
		public void setName(String name) {Name = name;}
		public String getStyle() {return Style;}
		public String getTitel() {return Titel;}
		public void setTitel(String titel) {Titel = titel;}
		public double getTileMaxSize() {return tileMaxSize;}
		public void setTileMaxSize(double tileMaxSize) {this.tileMaxSize = tileMaxSize;}
		public double getTileMinSize() {return TileMinSize;}
		public void setTileMinSize(double tileMinSize) {TileMinSize = tileMinSize;}
		public boolean isSplitTilesOnHighestLOD() {return splitTilesOnHighestLOD;}
		public void setSplitTilesOnHighestLOD(boolean splitTilesOnHighestLOD) {this.splitTilesOnHighestLOD = splitTilesOnHighestLOD;};
	}
//------------------------------------------------------------------------
	///////////////////////////////////////
	//*** Class Style ***
	public class Style {
		String Name; String Titel; String Abstract;

		public Style() {
			Name = "--NO NAME DEFINED--"; Titel = "--NO TITLE DEFINED--"; Abstract = "--NO DESCRIPTION DEFINED--";
		}
		public String getAbstract() {return Abstract;}
		public void setAbstract(String abstract1) {Abstract = abstract1;}
		public String getName() {return Name;}
		public void setName(String name) {Name = name;}
		public String getTitel() {return Titel;}
		public void setTitel(String titel) {Titel = titel;};
	}
//------------------------------------------------------------------------

/*
	//TESTER
	public static void main(String[] args) {	
		try {
				///////////////////////////////////////
				//*** Send Request to WMS ***
				//Send data
				URL urlWMS = new URL("http://webmap:8080/geoserver/wms?service=WMS&request=GetCapabilities");
				InputStream is = urlWMS.openStream();
        		SAXBuilder builder = new SAXBuilder();
        		Document doc = builder.build(is);

				ReadOutCapabilities r = new ReadOutCapabilities(doc);

				System.out.println(r.getWMSInfos().Name);
				System.out.println(r.getLayerList().size());
				for(int i=0 ; i<r.getLayerList().size() ; i++){
					System.out.println("Name: "+r.getLayerList().get(i).Name);
					System.out.println(" Style: "+r.getLayerList().get(i).Style);
				}
				
        		is.close();
        		System.out.println("END");

			} catch (Exception e) {
				System.out.println("- GetCapabilites to WMS ERROR-Exception: -");
			}
		}
*/
}
