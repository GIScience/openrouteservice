

package org.freeopenls.sld;

import java.math.BigDecimal;

import javax.xml.namespace.QName;

import net.opengis.gml.CoordType;
import net.opengis.gml.PointDocument;
import net.opengis.gml.PointType;
import net.opengis.sld.ParameterValueType;
import net.opengis.sld.SymbolizerType;
import net.opengis.sld.CssParameterDocument.CssParameter;
import net.opengis.sld.DisplacementDocument.Displacement;
import net.opengis.sld.ExternalGraphicDocument.ExternalGraphic;
import net.opengis.sld.FeatureTypeStyleDocument.FeatureTypeStyle;
import net.opengis.sld.FillDocument.Fill;
import net.opengis.sld.FontDocument.Font;
import net.opengis.sld.GraphicDocument.Graphic;
import net.opengis.sld.InlineFeatureDocument.InlineFeature;
import net.opengis.sld.LabelPlacementDocument.LabelPlacement;
import net.opengis.sld.LayerFeatureConstraintsDocument.LayerFeatureConstraints;
import net.opengis.sld.MarkDocument.Mark;
import net.opengis.sld.OnlineResourceDocument.OnlineResource;
import net.opengis.sld.PointPlacementDocument.PointPlacement;
import net.opengis.sld.PointSymbolizerDocument.PointSymbolizer;
import net.opengis.sld.RuleDocument.Rule;
import net.opengis.sld.StyledLayerDescriptorDocument.StyledLayerDescriptor;
import net.opengis.sld.TextSymbolizerDocument.TextSymbolizer;
import net.opengis.sld.UserLayerDocument.UserLayer;
import net.opengis.sld.UserStyleDocument.UserStyle;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * <p><b>Title: FileDelete</b></p>
 * <p><b>Description:</b>Class for generate and add SLD for Point UserLayer and UserStyle<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-06-14
 */
public class UserLayerPoint {
	/** Default SpatialReferenceSystem in which the Coordinate transform to */
	private String SRSName = "";

	/**
	 * Constructor - Set SpatialReferenceSystem
	 * @param sSRSName
	 */
	public UserLayerPoint(String sSRSName) {
		this.SRSName = sSRSName;
	}

	/**
	 * Method that add to the delivered StyledLayerDescriptor one layer with one Point (Circle-Symbol)
	 * 
	 * @param styledescr
	 * @param c
	 * 			Coordinate of the Point
	 * @param sLayerName
	 * @param sColor
	 * @param sTextSize
	 * @param sSymbolSize
	 * @throws XmlException
	 */
	public void addPoint(StyledLayerDescriptor styledescr, Coordinate c, String sLayerName, String sColor, String sTextSize, String sSymbolSize, String sOpacity)throws XmlException{
		//Add New Layer
		UserLayer userLayerPoint = styledescr.addNewUserLayer();
		userLayerPoint.setName(sLayerName);
		InlineFeature inlineFeatStart = userLayerPoint.addNewInlineFeature();

		//Create Point
		PointDocument pointDoc = PointDocument.Factory.newInstance();
		PointType pointType = pointDoc.addNewPoint();
		pointType.setSrsName(this.SRSName);
		CoordType coordStart = pointType.addNewCoord();
		coordStart.setX(new BigDecimal(c.x));
		coordStart.setY(new BigDecimal(c.y));
		
		String sStart = "<Point><the_geom>";
		String sStartEnd = "</the_geom><name>"+sLayerName+"</name></Point>";
		XmlObject oStart = XmlObject.Factory.parse(sStart + pointDoc + sStartEnd);
		inlineFeatStart.set(oStart);

		// LayerFeatureConstraints
		LayerFeatureConstraints layerfeat = userLayerPoint.addNewLayerFeatureConstraints();
		layerfeat.addNewFeatureTypeConstraint();

		// UserStyle
		UserStyle userStlye = userLayerPoint.addNewUserStyle();
		FeatureTypeStyle featStyle = userStlye.addNewFeatureTypeStyle();
		Rule rule = featStyle.addNewRule();
		rule.setName("Rule");
		rule.setTitle(sLayerName);

		//Symbolizer
			//PointSymbolizer
		SymbolizerType sym[] = new SymbolizerType[2];
		PointSymbolizer pointsym = PointSymbolizer.Factory.newInstance();
		Graphic graphic = pointsym.addNewGraphic();
		Mark mark = graphic.addNewMark();
		mark.setWellKnownName("circle");
			//Set Color
		Fill fill = mark.addNewFill();
		CssParameter css = fill.addNewCssParameter();
		css.setName("fill");
		XmlCursor cursorPoint = css.newCursor();
		cursorPoint.setTextValue(sColor);
		cursorPoint.dispose();
			//Set Opacity
		CssParameter css02 = fill.addNewCssParameter();
		css02.setName("fill-opacity");
		XmlCursor cursorOpacity = css02.newCursor();
		cursorOpacity.setTextValue(sOpacity);
		cursorOpacity.dispose();
		
		ParameterValueType paramSize = graphic.addNewSize();
		XmlCursor cursor02 = paramSize.newCursor();
		cursor02.setTextValue(sSymbolSize);
		cursor02.dispose();

			//TextSymbolizer
		TextSymbolizer textsym = TextSymbolizer.Factory.newInstance();
		ParameterValueType param = textsym.addNewLabel();
		String s = "<ogc:PropertyName xmlns:ogc=\"http://www.opengis.net/ogc\">name</ogc:PropertyName>";
		param.set(XmlObject.Factory.parse(s));
		
		Font font = textsym.addNewFont();
		CssParameter cssText01 = font.addNewCssParameter();
		cssText01.setName("font-family"); XmlCursor cursorText01 = cssText01.newCursor(); cursorText01.setTextValue("Arial"); cursorText01.dispose();
		CssParameter cssText02 = font.addNewCssParameter();
		cssText02.setName("font-style"); XmlCursor cursorText02 = cssText02.newCursor(); cursorText02.setTextValue("Bold"); cursorText02.dispose();
		CssParameter cssText03 = font.addNewCssParameter();
		cssText03.setName("font-size"); XmlCursor cursorText03 = cssText03.newCursor(); cursorText03.setTextValue(sTextSize); cursorText03.dispose();

			//Label/TextPlacement
		LabelPlacement labelpace = textsym.addNewLabelPlacement();
		PointPlacement pointplace = labelpace.addNewPointPlacement();
		Displacement displace = pointplace.addNewDisplacement();
		ParameterValueType paramDisplaceX = displace.addNewDisplacementX();
		XmlCursor cursor03 = paramDisplaceX.newCursor();
		cursor03.setTextValue("6");
		cursor03.dispose();
		ParameterValueType paramDisplaceY = displace.addNewDisplacementY();
		XmlCursor cursor04 = paramDisplaceY.newCursor();
		cursor04.setTextValue("0");
		cursor04.dispose();
		
		Fill fillText = textsym.addNewFill();
		CssParameter cssText10 = fillText.addNewCssParameter();
		cssText10.setName("fill"); XmlCursor cursorText10 = cssText10.newCursor(); cursorText10.setTextValue(sColor); cursorText10.dispose();

		sym[0] = (SymbolizerType) pointsym;
		sym[1] = (SymbolizerType) textsym;
		rule.setSymbolizerArray(sym);
		
	//--- For well formed XML / Symbolizer -> Point-/TextSymbolizer
		XmlCursor cursorRule01 = rule.newCursor();
		if (cursorRule01.toChild(new QName("http://www.opengis.net/sld", "Symbolizer"))) {
			cursorRule01.setName(new QName("http://www.opengis.net/sld", "PointSymbolizer"));
		}
		XmlCursor cursorRule02 = rule.newCursor();
		if (cursorRule02.toChild(new QName("http://www.opengis.net/sld", "Symbolizer"))) {
			cursorRule02.setName(new QName("http://www.opengis.net/sld", "TextSymbolizer"));
		}
		cursorRule01.dispose();
		cursorRule02.dispose();
	//---
	}

	/**
	 * Method that add to the delivered StyledLayerDescriptor one layer with one Point <br>
	 * and external graphic symbol.
	 * 
	 * @param styledescr
	 * @param c
	 * 			Coordinate of the Point
	 * @param sLayerName
	 * @param sColor
	 * @param sTextSize
	 * @param sSymbolSize
	 * @param sSymbolPath
	 * 			Path to the Symbol
	 * @param sSymbolFormat
	 * @throws XmlException
	 */
	public void addPointGraphic(StyledLayerDescriptor styledescr, Coordinate c, String sLayerName, String sColor, String sTextSize, String sSymbolSize, String sSymbolPath, String sSymbolFormat)throws XmlException{

		//Add New Layer
		UserLayer userLayer = styledescr.addNewUserLayer();
		userLayer.setName(sLayerName);
		InlineFeature inlineFeatEnd = userLayer.addNewInlineFeature();

		//Create Point
		PointDocument pointDoc = PointDocument.Factory.newInstance();
		PointType pointType = pointDoc.addNewPoint();
		pointType.setSrsName(this.SRSName);
		CoordType coord = pointType.addNewCoord();
		coord.setX(new BigDecimal(c.x));
		coord.setY(new BigDecimal(c.y));
		
		String sTMP = "<Point><the_geom>";
		String sTMPEnd = "</the_geom><name>"+sLayerName+"</name></Point>";
		XmlObject oEnd = XmlObject.Factory.parse(sTMP + pointDoc + sTMPEnd);
		inlineFeatEnd.set(oEnd);

		// LayerFeatureConstraints
		LayerFeatureConstraints layerfeat = userLayer.addNewLayerFeatureConstraints();
		layerfeat.addNewFeatureTypeConstraint();

		// UserStyle
		UserStyle userStlye = userLayer.addNewUserStyle();
		FeatureTypeStyle featStyle = userStlye.addNewFeatureTypeStyle();
		Rule rule = featStyle.addNewRule();
		rule.setName("Rule");
		rule.setTitle(sLayerName);

		SymbolizerType sym[] = new SymbolizerType[2];
		PointSymbolizer pointsym = PointSymbolizer.Factory.newInstance();
		Graphic graphic = pointsym.addNewGraphic();
		ExternalGraphic exgraphic = graphic.addNewExternalGraphic();
		OnlineResource onlineres = exgraphic.addNewOnlineResource();
		XmlCursor cursoronlineres = onlineres.newCursor();
		cursoronlineres.setAttributeText(new QName("xmlns","xlink","xmlns"), "http://www.w3.org/1999/xlink");
		cursoronlineres.dispose();
		onlineres.setHref(sSymbolPath);
		onlineres.setType("simple");
		exgraphic.setFormat(sSymbolFormat);
		
		ParameterValueType paramSize = graphic.addNewSize();
		XmlCursor cursor02 = paramSize.newCursor();
		cursor02.setTextValue(sSymbolSize);
		cursor02.dispose();
		
		TextSymbolizer textsym = TextSymbolizer.Factory.newInstance();
		ParameterValueType param = textsym.addNewLabel();
		param.set(XmlObject.Factory.parse("<ogc:PropertyName xmlns:ogc=\"http://www.opengis.net/ogc\">name</ogc:PropertyName>"));
		
		Font font = textsym.addNewFont();
		CssParameter cssText01 = font.addNewCssParameter();
		cssText01.setName("font-family"); XmlCursor cursorText01 = cssText01.newCursor(); cursorText01.setTextValue("Arial"); cursorText01.dispose();
		CssParameter cssText02 = font.addNewCssParameter();
		cssText02.setName("font-style"); XmlCursor cursorText02 = cssText02.newCursor(); cursorText02.setTextValue("Bold"); cursorText02.dispose();
		CssParameter cssText03 = font.addNewCssParameter();
		cssText03.setName("font-size"); XmlCursor cursorText03 = cssText03.newCursor(); cursorText03.setTextValue(sTextSize); cursorText03.dispose();

		LabelPlacement labelpace = textsym.addNewLabelPlacement();
		PointPlacement pointplace = labelpace.addNewPointPlacement();
		Displacement displace = pointplace.addNewDisplacement();
		ParameterValueType paramDisplaceX = displace.addNewDisplacementX();
		XmlCursor cursor03 = paramDisplaceX.newCursor();
		cursor03.setTextValue("6");
		cursor03.dispose();
		ParameterValueType paramDisplaceY = displace.addNewDisplacementY();
		XmlCursor cursor04 = paramDisplaceY.newCursor();
		cursor04.setTextValue("-5");
		cursor04.dispose();
		
		Fill fillText = textsym.addNewFill();
		CssParameter cssText10 = fillText.addNewCssParameter();
		cssText10.setName("fill"); XmlCursor cursorText10 = cssText10.newCursor(); cursorText10.setTextValue(sColor); cursorText10.dispose();

		sym[0] = (SymbolizerType) pointsym;
		sym[1] = (SymbolizerType) textsym;
		rule.setSymbolizerArray(sym);
		
	//--- For well formed XML / Symbolizer -> Point-/TextSymbolizer
		XmlCursor cursorRule01 = rule.newCursor();
		if (cursorRule01.toChild(new QName("http://www.opengis.net/sld", "Symbolizer"))) {
			cursorRule01.setName(new QName("http://www.opengis.net/sld", "PointSymbolizer"));
		}
		XmlCursor cursorRule02 = rule.newCursor();
		if (cursorRule02.toChild(new QName("http://www.opengis.net/sld", "Symbolizer"))) {
			cursorRule02.setName(new QName("http://www.opengis.net/sld", "TextSymbolizer"));
		}
		cursorRule01.dispose();
		cursorRule02.dispose();
	//---
	}
}
