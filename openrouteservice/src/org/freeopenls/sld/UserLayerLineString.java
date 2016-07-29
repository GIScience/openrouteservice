

package org.freeopenls.sld;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import net.opengis.gml.CoordType;
import net.opengis.gml.LineStringDocument;
import net.opengis.gml.LineStringType;
import net.opengis.sld.CssParameterDocument.CssParameter;
import net.opengis.sld.FeatureTypeStyleDocument.FeatureTypeStyle;
import net.opengis.sld.InlineFeatureDocument.InlineFeature;
import net.opengis.sld.LayerFeatureConstraintsDocument.LayerFeatureConstraints;
import net.opengis.sld.LineSymbolizerDocument.LineSymbolizer;
import net.opengis.sld.RuleDocument.Rule;
import net.opengis.sld.StrokeDocument.Stroke;
import net.opengis.sld.StyledLayerDescriptorDocument.StyledLayerDescriptor;
import net.opengis.sld.UserLayerDocument.UserLayer;
import net.opengis.sld.UserStyleDocument.UserStyle;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * <p><b>Title: UserLayerLineString</b></p>
 * <p><b>Description:</b> Class for GetMap WMS.<br>
 * Defines UserLayer with LineString.<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 2.0 2007-02-05
 */
public class UserLayerLineString {
	/** Default SpatialReferenceSystem in which the Coordinate transform to */
	private String mSRSName = "";

	/**
	 * Constructor - Set SpatialReferenceSystem
	 * @param sSRSName
	 */
	public UserLayerLineString(String sSRSName) {
		mSRSName = sSRSName;
	}

	/**
	 * Method that add to the delivered StyledLayerDescriptor one layer with several LineStrings
	 * 
	 * @param styledescr
	 * @param sLayername
	 * @param alRoute
	 * @param sColor
	 * @param sWidth
	 * @param sOpacity
	 * @throws XmlException
	 */
	public void addLineStrings(StyledLayerDescriptor styledescr, String sLayername, ArrayList<Coordinate> alRoute, String sColor, String sWidth, String sOpacity) throws XmlException {
		//New UserLayer
		UserLayer userLayer = styledescr.addNewUserLayer();
		userLayer.setName(sLayername);
		InlineFeature inlineFeat = userLayer.addNewInlineFeature();

		LineStringDocument lsDoc = LineStringDocument.Factory.newInstance();
		LineStringType lsType = lsDoc.addNewLineString();
		lsType.setSrsName(mSRSName);

		for (int j = 0 ; j < alRoute.size() ; j++) {
			CoordType direct = lsType.addNewCoord();
			direct.setX(new BigDecimal(alRoute.get(j).x).setScale(7, RoundingMode.HALF_UP));
			direct.setY(new BigDecimal(alRoute.get(j).y).setScale(7, RoundingMode.HALF_UP));
		}
		String sDocTMP = "<featureMember><Line><linestringProperty>"+lsDoc+"</linestringProperty></Line></featureMember>";
		
		String sTest = "<FeatureCollection>";
		String sTestEnd = "</FeatureCollection>";
		XmlObject otest = XmlObject.Factory.parse(sTest + sDocTMP + sTestEnd);
		inlineFeat.set(otest);

		// LayerFeatureConstraints
		LayerFeatureConstraints layerfeat = userLayer.addNewLayerFeatureConstraints();
		layerfeat.addNewFeatureTypeConstraint();

		// UserStyle
		UserStyle userStlye = userLayer.addNewUserStyle();
		FeatureTypeStyle featStyle = userStlye.addNewFeatureTypeStyle();
		Rule rule = featStyle.addNewRule();

		//Add LineSymbolizer
		LineSymbolizer linesym[] = new LineSymbolizer[1];
		linesym[0] = LineSymbolizer.Factory.newInstance();

			//Set Color
		Stroke stroke = linesym[0].addNewStroke();
		CssParameter css01 = stroke.addNewCssParameter();
		css01.setName("stroke");
		XmlCursor cursor01 = css01.newCursor();
		cursor01.setTextValue(sColor);
		cursor01.dispose();

			//Set Size/Width
		CssParameter css02 = stroke.addNewCssParameter();
		css02.setName("stroke-width");
		XmlCursor cursor02 = css02.newCursor();
		cursor02.setTextValue(sWidth);
		cursor02.dispose();
		
			//Set Opacity
		CssParameter css03 = stroke.addNewCssParameter();
		css03.setName("stroke-opacity");
		XmlCursor cursor03 = css03.newCursor();
		cursor03.setTextValue(sOpacity);
		cursor03.dispose();

		rule.setSymbolizerArray(linesym);
		
	// ---- For well formed XML / Symbolizer -> LineSymbolizer
		XmlCursor cursor = rule.newCursor();
		if (cursor.toChild(new QName("http://www.opengis.net/sld", "Symbolizer"))) {
			cursor.setName(new QName("http://www.opengis.net/sld", "LineSymbolizer"));
		}
		cursor.dispose();
	// ----
	}
}
