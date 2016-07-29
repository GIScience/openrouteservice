

package org.freeopenls.sld;

import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.namespace.QName;

import net.opengis.ogc.FeatureIdType;
import net.opengis.ogc.FilterType;
import net.opengis.sld.ParameterValueType;
import net.opengis.sld.CssParameterDocument.CssParameter;
import net.opengis.sld.FeatureTypeStyleDocument.FeatureTypeStyle;
import net.opengis.sld.FillDocument.Fill;
import net.opengis.sld.FontDocument.Font;
import net.opengis.sld.LabelPlacementDocument.LabelPlacement;
import net.opengis.sld.LinePlacementDocument.LinePlacement;
import net.opengis.sld.NamedLayerDocument.NamedLayer;
import net.opengis.sld.PolygonSymbolizerDocument.PolygonSymbolizer;
import net.opengis.sld.RuleDocument.Rule;
import net.opengis.sld.StyledLayerDescriptorDocument.StyledLayerDescriptor;
import net.opengis.sld.TextSymbolizerDocument.TextSymbolizer;
import net.opengis.sld.UserStyleDocument.UserStyle;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;


/**
 * <p><b>Title: NamedLayerUserStyle</b></p>
 * <p><b>Description:</b> Class for GetMap WMS.<br>
 * Defines NamedLayer and UserStyle<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-06-11
 */
public class NamedLayerUserStyle {

	/**
	 * Method that add a NamedLayer and with a UserStlye to the StyledLayerDesriptor Doc
	 * 
	 * @param hsFeatIds
	 * @param sColor
	 * @param sOpacity
	 * @return UserStyle
	 * @throws XmlException
	 */
	public UserStyle createUserStlyePolygon(HashSet<String> hsFeatIds, String sColor, String sOpacity)throws XmlException{
		UserStyle userStyle = UserStyle.Factory.newInstance();
		
		FeatureTypeStyle featStyle = userStyle.addNewFeatureTypeStyle();
		Rule rule = featStyle.addNewRule();
		
		FilterType filter = rule.addNewFilter();
		String sFeatureIDs = "<Or xmlns:ogc=\"http://www.opengis.net/ogc\">";
		Object[] objArray = hsFeatIds.toArray();
		for(int i=0 ; i<objArray.length ; i++){
			FeatureIdType featureID = FeatureIdType.Factory.newInstance();
			featureID.setFid((String)objArray[i]);
			sFeatureIDs =  sFeatureIDs +"\n"+ featureID.toString();
		}
		sFeatureIDs = sFeatureIDs.replaceAll("xml-fragment","ogc:FeatureId xmlns:ogc=\"http://www.opengis.net/ogc\"");
		filter.set(XmlObject.Factory.parse(sFeatureIDs+"</Or>"));
		
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
		cursor20.setTextValue(sOpacity);//"1");
		cursor20.dispose();
		
		rule.setSymbolizerArray(polygonsym);
		
		// ------For well formed XML / Symbolizer -> PolygonSymbolizer
			XmlCursor cursorRule = rule.newCursor();
			if (cursorRule.toChild(new QName("http://www.opengis.net/sld", "Symbolizer"))) {
				cursorRule.setName(new QName("http://www.opengis.net/sld", "PolygonSymbolizer"));
			}
			XmlCursor cursorFilter = filter.newCursor();
			if (cursorFilter.toChild(new QName("http://www.opengis.net/ogc", "logicOps"))) {
				cursorFilter.setName(new QName("http://www.opengis.net/ogc", "Or"));
			}
			cursorRule.dispose();
			cursorFilter.dispose();
		// ------
		
		return userStyle;
	}

	/**
	 * Method that add a NamedLayer and with a UserStlye to the StyledLayerDesriptor Doc
	 * 
	 * @param styledescr
	 * @param sLayerName
	 * @param alFeatureIds
	 * @param sColor
	 * @param sOpacity
	 * @throws XmlException
	 */
	public void addNamedLayerUserStyleText(StyledLayerDescriptor styledescr, String sLayerName, ArrayList<String> alFeatureIds, String sColor, String sOpacity)throws XmlException{
		NamedLayer namLayer22 = styledescr.addNewNamedLayer();
		namLayer22.setName(sLayerName);
		UserStyle userStyle = namLayer22.addNewUserStyle();
		
		FeatureTypeStyle featStyle = userStyle.addNewFeatureTypeStyle();
		Rule rule = featStyle.addNewRule();
		
		FilterType filter = rule.addNewFilter();
		String sFeatureIDs = "<Or xmlns:ogc=\"http://www.opengis.net/ogc\">";
		for(int i=0 ; i<alFeatureIds.size() ; i++){
			FeatureIdType featureID = FeatureIdType.Factory.newInstance();
			featureID.setFid(alFeatureIds.get(i));
			sFeatureIDs =  sFeatureIDs +"\n"+ featureID.toString();
		}
		sFeatureIDs = sFeatureIDs.replaceAll("xml-fragment","ogc:FeatureId xmlns:ogc=\"http://www.opengis.net/ogc\"");
		filter.set(XmlObject.Factory.parse(sFeatureIDs+"</Or>"));

			//TextSymbolizer
		TextSymbolizer textsym[] = new TextSymbolizer[1];
		textsym[0] = TextSymbolizer.Factory.newInstance();
		ParameterValueType param = textsym[0].addNewLabel();
		String s = "<ogc:PropertyName xmlns:ogc=\"http://www.opengis.net/ogc\">strName</ogc:PropertyName>";
		param.set(XmlObject.Factory.parse(s));
		
		Font fontStart = textsym[0].addNewFont();
		CssParameter cssText01 = fontStart.addNewCssParameter();
		cssText01.setName("font-family"); XmlCursor cursorText01 = cssText01.newCursor(); cursorText01.setTextValue("Arial"); cursorText01.dispose();
		CssParameter cssText02 = fontStart.addNewCssParameter();
		cssText02.setName("font-style"); XmlCursor cursorText02 = cssText02.newCursor(); cursorText02.setTextValue("Bold"); cursorText02.dispose();
		CssParameter cssText03 = fontStart.addNewCssParameter();
		cssText03.setName("font-size"); XmlCursor cursorText03 = cssText03.newCursor(); cursorText03.setTextValue("8"); cursorText03.dispose();

			//Label/TextPlacement
		LabelPlacement labelpace = textsym[0].addNewLabelPlacement();
		LinePlacement lineplace = labelpace.addNewLinePlacement();
		ParameterValueType paramvalue = lineplace.addNewPerpendicularOffset();
		
		XmlCursor paramvaluecursor = paramvalue.newCursor();
		paramvaluecursor.setTextValue("0");
		paramvaluecursor.dispose();

		rule.setSymbolizerArray(textsym);
		
		// ------For well formed XML / Symbolizer -> PolygonSymbolizer
			XmlCursor cursorRule = rule.newCursor();
			if (cursorRule.toChild(new QName("http://www.opengis.net/sld", "Symbolizer"))) {
				cursorRule.setName(new QName("http://www.opengis.net/sld", "TextSymbolizer"));
			}
			XmlCursor cursorFilter = filter.newCursor();
			if (cursorFilter.toChild(new QName("http://www.opengis.net/ogc", "logicOps"))) {
				cursorFilter.setName(new QName("http://www.opengis.net/ogc", "Or"));
			}
			cursorRule.dispose();
			cursorFilter.dispose();
		// ------
	}
}
