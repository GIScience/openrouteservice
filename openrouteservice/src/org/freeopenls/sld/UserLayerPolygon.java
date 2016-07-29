
package org.freeopenls.sld;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.xml.namespace.QName;

import net.opengis.gml.AbstractRingPropertyType;
import net.opengis.gml.AbstractRingType;
import net.opengis.gml.CoordType;
import net.opengis.gml.LinearRingType;
import net.opengis.gml.PolygonDocument;
import net.opengis.gml.PolygonType;
import net.opengis.sld.CssParameterDocument.CssParameter;
import net.opengis.sld.FeatureTypeStyleDocument.FeatureTypeStyle;
import net.opengis.sld.FillDocument.Fill;
import net.opengis.sld.InlineFeatureDocument.InlineFeature;
import net.opengis.sld.LayerFeatureConstraintsDocument.LayerFeatureConstraints;
import net.opengis.sld.PolygonSymbolizerDocument.PolygonSymbolizer;
import net.opengis.sld.RuleDocument.Rule;
import net.opengis.sld.StyledLayerDescriptorDocument.StyledLayerDescriptor;
import net.opengis.sld.UserLayerDocument.UserLayer;
import net.opengis.sld.UserStyleDocument.UserStyle;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;


/**
 * <p><b>Title: UserLayerPolygon</b></p>
 * <p><b>Description:</b> Class for UserLayerPolygon<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-07-01
 */
public class UserLayerPolygon {
	/** Default SpatialReferenceSystem in which the Coordinate transform to */
	private String SRSName = "";

	/**
	 * Constructor - Set SpatialReferenceSystem
	 * @param sSRSName
	 */
	public UserLayerPolygon(String sSRSName) {
		this.SRSName = sSRSName;
	}

	/**
	 * Method that add to the delivered StyledLayerDescriptor one layer with one or several Polygons
	 * 
	 * @param styledescr
	 * @param sLayername
	 * @param polygon
	 * @param sColor
	 * @param sOpacity
	 * @throws XmlException
	 */
	public void addPolygons(StyledLayerDescriptor styledescr, String sLayername, Polygon polygon, String sColor, String sOpacity) throws XmlException {
		//New User layer
		UserLayer userLayer = styledescr.addNewUserLayer();
		userLayer.setName(sLayername);
		InlineFeature inlineFeat = userLayer.addNewInlineFeature();

		String sDocTMP = "";
		
		//Add Geometry(Polygons) to the Layer
		
			PolygonDocument polyDoc = PolygonDocument.Factory.newInstance();
			PolygonType polygonType = polyDoc.addNewPolygon();
			polygonType.setSrsName(this.SRSName);
			
			//EXTERIOR
			AbstractRingPropertyType ringpropOUT = polygonType.addNewExterior();
			AbstractRingType ring = ringpropOUT.addNewRing();
			LinearRingType linearring = (LinearRingType) ring.changeType(LinearRingType.type);
			
			LineString lsEx = polygon.getExteriorRing();
			Coordinate c[] = lsEx.getCoordinates();

			for (int j2 = 0; j2 < c.length; j2++) {
				CoordType direct = linearring.addNewCoord();
				direct.setX(new BigDecimal(c[j2].x).setScale(7, RoundingMode.HALF_UP));
				direct.setY(new BigDecimal(c[j2].y).setScale(7, RoundingMode.HALF_UP));
			}
			
			//INTERIOR
			if(polygon.getNumInteriorRing()>0){
				int iInteriorRings = polygon.getNumInteriorRing();
				
				AbstractRingPropertyType  ringpropIN = polygonType.addNewInterior();
				
				for(int iRingIndex=0 ; iRingIndex<iInteriorRings ; iRingIndex++){	
					AbstractRingType ringIN = ringpropIN.addNewRing();
					LinearRingType linearringIN = (LinearRingType) ringIN.changeType(LinearRingType.type);
					
					LineString lsInterior = polygon.getInteriorRingN(iRingIndex);
					Coordinate cIN[] = lsInterior.getCoordinates();
	
					for (int j2 = 0; j2 < cIN.length; j2++) {
						CoordType direct = linearringIN.addNewCoord();
						direct.setX(new BigDecimal(cIN[j2].x).setScale(7, RoundingMode.HALF_UP));
						direct.setY(new BigDecimal(cIN[j2].y).setScale(7, RoundingMode.HALF_UP));
					}
					
					XmlCursor cursorIN = ringpropIN.newCursor();
					if (cursorIN.toChild(new QName("http://www.opengis.net/gml", "_Ring"))) {
						cursorIN.setName(new QName("http://www.opengis.net/gml", "LinearRing"));
					}
					cursorIN.dispose();
				}
				XmlCursor cursor55 = polygonType.newCursor();
				if (cursor55.toChild(new QName("http://www.opengis.net/gml", "interior"))) {
					cursor55.setName(new QName("http://www.opengis.net/gml", "innerBoundaryIs"));
				}
				XmlCursor cursor56 = polygonType.newCursor();
				if (cursor56.toChild(new QName("", "xml-fragment"))) {
					cursor56.setName(new QName("http://www.opengis.net/gml", "innerBoundaryIs"));
				}
				cursor55.dispose();
				cursor56.dispose();
			}

		// ------For well formed XML / Symbolizer -> LineSymbolizer
			XmlCursor cursorIN = ringpropOUT.newCursor();
			if (cursorIN.toChild(new QName("http://www.opengis.net/gml", "_Ring"))) {
				cursorIN.setName(new QName("http://www.opengis.net/gml", "LinearRing"));
			}
			XmlCursor cursor02 = polygonType.newCursor();
			if (cursor02.toChild(new QName("http://www.opengis.net/gml", "exterior"))) {
				cursor02.setName(new QName("http://www.opengis.net/gml", "outerBoundaryIs"));
			}
			XmlCursor cursor03 = polygonType.newCursor();
			if (cursor03.toChild(new QName("", "xml-fragment"))) {
				cursor03.setName(new QName("http://www.opengis.net/gml", "outerBoundaryIs"));
			}
			cursorIN.dispose();
			cursor02.dispose();
			cursor03.dispose();
		// ------
			
			sDocTMP = sDocTMP+"<FeatureMember>"+polyDoc+"</FeatureMember>";

		String sTest = "<FeatureCollection>";
		String sTestEnd = "</FeatureCollection>";
		XmlObject otest = XmlObject.Factory.parse(sTest + sDocTMP + sTestEnd);
		inlineFeat.set(otest);
	
		// LayerFeatureConstraints
		LayerFeatureConstraints layerfeat = userLayer.addNewLayerFeatureConstraints();
		layerfeat.addNewFeatureTypeConstraint();

		//Add UserStyle
		UserStyle userStlye = userLayer.addNewUserStyle();
		FeatureTypeStyle featStyle = userStlye.addNewFeatureTypeStyle();
		Rule rule = featStyle.addNewRule();

		//PolygonSymbolizer
		PolygonSymbolizer polygonsym[] = new PolygonSymbolizer[1];
		polygonsym[0] = PolygonSymbolizer.Factory.newInstance();

			//Set Color
		Fill fill = polygonsym[0].addNewFill();
		CssParameter css01 = fill.addNewCssParameter();
		css01.setName("fill");
		XmlCursor cursor01 = css01.newCursor();
		cursor01.setTextValue(sColor);
		cursor01.dispose();
			//Set Opacity
		CssParameter css02 = fill.addNewCssParameter();
		css02.setName("fill-opacity");
		XmlCursor cursor20 = css02.newCursor();
		cursor20.setTextValue(sOpacity);
		cursor20.dispose();

		rule.setSymbolizerArray(polygonsym);
		
	// ------For well formed XML / Symbolizer -> LineSymbolizer
		XmlCursor cursor22 = rule.newCursor();
		if (cursor22.toChild(new QName("http://www.opengis.net/sld", "Symbolizer"))) {
			cursor22.setName(new QName("http://www.opengis.net/sld", "PolygonSymbolizer"));
		}
		cursor22.dispose();
	// ------
	}
}
