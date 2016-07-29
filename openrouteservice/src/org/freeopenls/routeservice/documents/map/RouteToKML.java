
package org.freeopenls.routeservice.documents.map;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.freeopenls.error.ServiceError;
import org.freeopenls.tools.CoordTransform;

import com.google.earth.kml.x21.DocumentType;
import com.google.earth.kml.x21.FeatureType;
import com.google.earth.kml.x21.GeometryType;
import com.google.earth.kml.x21.IconStyleIconType;
import com.google.earth.kml.x21.IconStyleType;
import com.google.earth.kml.x21.KmlDocument;
import com.google.earth.kml.x21.KmlType;
import com.google.earth.kml.x21.LineStringType;
import com.google.earth.kml.x21.LineStyleType;
import com.google.earth.kml.x21.PlacemarkType;
import com.google.earth.kml.x21.PointType;
import com.google.earth.kml.x21.StyleSelectorType;
import com.google.earth.kml.x21.StyleType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;


/**
 * <p><b>Title: RouteToKML</b></p>
 * <p><b>Description:</b> Class for create KML file<br>
 * <br>
 * TODO: Klasse muss noch überarbeitet werden!!!<br>
 * - Pretty Print ...<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 0.9 2007-12-25
 */
public class RouteToKML {
	
	/**
	 * Method that create a KML File with the Route to the given file-path
	 * 
	 * @param sFilePath
	 * @param sFileName
	 * @param sFeatureName
	 * @param featcollRoute
	 */
	public static void createKML(String sFilePath, String sFileName, String sFeatureName, FeatureCollection featcollRoute,
			String sRouteSRS)throws ServiceError{
		
		//Coordinates start and end Route
		Coordinate cStart = new Coordinate();
		Coordinate cEnd = new Coordinate();
		
		//*** Create new KMLDokument ***
		KmlDocument kmldoc = KmlDocument.Factory.newInstance();
		KmlType kml = kmldoc.addNewKml();
		FeatureType feat = kml.addNewFeature();
		//Document
		DocumentType doc = (DocumentType)feat.changeType(DocumentType.type);
		doc.setName(sFileName);
		doc.setOpen(true);//Attention: This Value is set by the XML-Curors at the end!
		//Style
		StyleSelectorType styleselecroute = doc.addNewStyleSelector();
		StyleType styleroute = (StyleType) styleselecroute.changeType(StyleType.type);
		styleroute.setId("routestyle");
		LineStyleType lsStyle = styleroute.addNewLineStyle();
			String sColor = "7d0000ff";
			lsStyle.setColor(sColor.getBytes());
			lsStyle.setWidth(3f);
		StyleSelectorType styleselecend = doc.addNewStyleSelector();
		StyleType styleend = (StyleType) styleselecend.changeType(StyleType.type);
			styleend.setId("endstyle");
		IconStyleType iconStyle = styleend.addNewIconStyle();
			iconStyle.setScale(1.4f);
			IconStyleIconType iconType = iconStyle.addNewIcon();
			iconType.setHref("http://131.220.111.120:8080/geoserver/data/symbols/endflag.png");
		
		//Add Route
		FeatureType featroute = doc.addNewFeature();
		PlacemarkType placeroute = (PlacemarkType)featroute.changeType(PlacemarkType.type);
		placeroute.setName(sFeatureName);
		placeroute.setStyleUrl("#routestyle");
		GeometryType geom = placeroute.addNewGeometry();
		LineStringType ls = (LineStringType) geom.changeType(LineStringType.type);
		ls.setExtrude(true);//Attention: This Value is set by the XML-Cursor at the end!
		ls.setTessellate(true);//Attention: This Value is set by the XML-Cursor at the end!
		
		List<String> coordlist = new Vector<String>();
		List l = featcollRoute.getFeatures();
		Feature firstfeat = (Feature) l.get(0);
		LineString firstlineTMP = (LineString) firstfeat.getGeometry();
		
		if(!sRouteSRS.equals("EPSG:4326"))
			cStart = CoordTransform.transformGetCoord(sRouteSRS, "EPSG:4326", firstlineTMP.getCoordinates()[0]);
		else
			cStart = firstlineTMP.getCoordinates()[0];
		
		coordlist.add(cStart.x+","+cStart.y+",0");		
		for (int i = 0; i < l.size(); i++) {
			Feature featTMP = (Feature) l.get(i);
			LineString lineTMP = (LineString) featTMP.getGeometry();
			Coordinate c[] = lineTMP.getCoordinates();
				
			for (int j = 1; j < c.length; j++) {
				if(!sRouteSRS.equals("EPSG:4326"))
					cEnd = CoordTransform.transformGetCoord(sRouteSRS, "EPSG:4326", c[j]);
				else
					cEnd = c[j];
				coordlist.add(+cEnd.x+","+cEnd.y+",0");
			}
		}
		//coordlist.add("37.2,122.04,0");
		//coordlist.add("37.3,122.05,0");
		ls.setCoordinates(coordlist);
		
		//Add Start Point
		FeatureType featstart = doc.addNewFeature();
		PlacemarkType placestart = (PlacemarkType)featstart.changeType(PlacemarkType.type);
		placestart.setName("Start");
		GeometryType geomstart = placestart.addNewGeometry();
		PointType pstart = (PointType) geomstart.changeType(PointType.type);
		List<String> coordlistStart = new Vector<String>();
		//coordlistStart.add("37.2,122.04,0");
		coordlistStart.add(cStart.x+","+cStart.y+",0");
		pstart.setCoordinates(coordlistStart);

		//Add End Point
		FeatureType featend = doc.addNewFeature();
		PlacemarkType placeend = (PlacemarkType)featend.changeType(PlacemarkType.type);
		placeend.setName("End");
		placeend.setStyleUrl("#endstyle");
		GeometryType geomend = placeend.addNewGeometry();
		PointType pend = (PointType) geomend.changeType(PointType.type);
		List<String> coordlistEnd = new Vector<String>();
		coordlistEnd.add(cEnd.x+","+cEnd.y+",0");
		//coordlistEnd.add("37.2,122.04,0");
		pend.setCoordinates(coordlistEnd);

		
		// ---- For well formed XML-Doc
			XmlCursor cursorKml = kml.newCursor();
			XmlCursor cursorDoc = doc.newCursor();
			XmlCursor cursorLS = ls.newCursor();
			XmlCursor lscursor = lsStyle.newCursor();
			
			if(lscursor.toChild(new QName("http://earth.google.com/kml/2.1", "color")))
				lscursor.setTextValue("7d0000ff");
			if (cursorKml.toChild(new QName("http://earth.google.com/kml/2.1", "Feature")))
				cursorKml.setName(new QName("http://earth.google.com/kml/2.1","Document"));
			if (cursorDoc.toChild(new QName("http://earth.google.com/kml/2.1", "Feature")))
				cursorDoc.setName(new QName("http://earth.google.com/kml/2.1","Placemark"));
			if (cursorDoc.toChild(new QName("http://earth.google.com/kml/2.1", "Geometry")))
				cursorDoc.setName(new QName("http://earth.google.com/kml/2.1","LineString"));
			cursorDoc.toParent();cursorDoc.toParent();//Cursor Reset!
			if (cursorDoc.toChild(new QName("http://earth.google.com/kml/2.1", "Feature")))
				cursorDoc.setName(new QName("http://earth.google.com/kml/2.1","Placemark"));
			if (cursorDoc.toChild(new QName("http://earth.google.com/kml/2.1", "Geometry")))
				cursorDoc.setName(new QName("http://earth.google.com/kml/2.1","Point"));
			cursorDoc.toParent();cursorDoc.toParent();//Cursor Reset!
			if (cursorDoc.toChild(new QName("http://earth.google.com/kml/2.1", "Feature")))
				cursorDoc.setName(new QName("http://earth.google.com/kml/2.1","Placemark"));
			if (cursorDoc.toChild(new QName("http://earth.google.com/kml/2.1", "Geometry")))
				cursorDoc.setName(new QName("http://earth.google.com/kml/2.1","Point"));
			if (cursorKml.toChild(new QName("http://earth.google.com/kml/2.1", "open")))
				cursorKml.setTextValue("1");
			cursorKml.toParent();//Cursor Reset!
			if (cursorKml.toChild(new QName("http://earth.google.com/kml/2.1", "StyleSelector")))
				cursorKml.setName(new QName("http://earth.google.com/kml/2.1","Style"));
			cursorKml.toParent();//Cursor Reset!
			if (cursorKml.toChild(new QName("http://earth.google.com/kml/2.1", "StyleSelector")))
				cursorKml.setName(new QName("http://earth.google.com/kml/2.1","Style"));
			if (cursorLS.toChild(new QName("http://earth.google.com/kml/2.1", "extrude")))
				cursorLS.setTextValue("1");
			cursorLS.toParent();//Cursor Reset!
			if (cursorLS.toChild(new QName("http://earth.google.com/kml/2.1", "tessellate")))
				cursorLS.setTextValue("1");
			
			lscursor.dispose();
			cursorKml.dispose();
			cursorDoc.dispose();
			cursorLS.dispose();
		// ----
		
		try{
			String sFile = sFilePath+"/"+sFileName;
			FileOutputStream buf = new FileOutputStream(sFile);
			String sOut = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+kmldoc.toString();
//System.out.println(sOut);
			buf.write(sOut.getBytes());
			buf.close();
			
		}catch(IOException ioe){
			System.out.println("ERROR: "+ioe);
		}
		
	}

//*** Tester ***
//	public static void main(String[] args) {
//		RouteToKML test = new RouteToKML();
//		test.createKML("","test.kml", "Route");
//	}
}
