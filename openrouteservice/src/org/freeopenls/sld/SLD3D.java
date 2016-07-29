

package org.freeopenls.sld;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.SeverityType;

import org.apache.log4j.Logger;
import org.freeopenls.error.ServiceError;
import org.freeopenls.tools.CoordTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;


/**
 * <p><b>Title: FileDelete</b></p>
 * <p><b>Description:</b> Class for FileDelete<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-12-10
 */
public class SLD3D {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger mLogger = Logger.getLogger(SLD3D.class.getName());
	
	public static String createSLD3D(String responseSRS, FeatureCollection featureCollection, String featureCollectionSRS,
			double dXLower, double dYLower, double dXUpper, double dYUpper,
			ArrayList<String> arraylistLayerNames,
			String filePath, String fileName)throws ServiceError{
		
		String sld3d = "";
		
		sld3d+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		sld3d+="<sld3d:GetScene \n";
		sld3d+=" xmlns:sld3d=\"http://www.opengis.net/sld3d\"\n";
		sld3d+=" xmlns:sld=\"http://www.opengis.net/sld\"\n";
		sld3d+=" xmlns:ogc=\"http://www.opengis.net/ogc\"\n";
		sld3d+=" xmlns:gml=\"http://www.opengis.net/gml\"\n";
		sld3d+=" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
		sld3d+=" xsi:schemaLocation=\"http://www.opengis.net/sld3d\"\n";
		sld3d+=" version=\"0.1.0\">\n";
		sld3d+="<sld3d:StyledLayerDescriptor version=\"0.1.0\">\n";
		sld3d+=" <Name xmlns=\"http://www.opengis.net/se\">OLS RS</Name>\n";
		sld3d+=" <sld3d:UserLayer>\n";
		sld3d+="	<Name xmlns=\"http://www.opengis.net/se\">poly</Name>\n";
		sld3d+="	<sld:InlineFeature>\n";
		sld3d+="		<gml:FeatureCollection>\n";
		sld3d+="			<gml:location>\n";
		sld3d+="				<gml:LineString srsName=\""+responseSRS+"\">\n";
		
		sld3d+= addLinePoints(responseSRS, featureCollection, featureCollectionSRS);
		
		sld3d+="				</gml:LineString>\n";
		sld3d+="			</gml:location>\n";
		sld3d+="		</gml:FeatureCollection>\n";
		sld3d+="	</sld:InlineFeature>\n";
		sld3d+=" 	<sld3d:UserStyle>\n";
		sld3d+=" 	 <Name xmlns=\"http://www.opengis.net/se\">yellow</Name>\n";
		sld3d+="	 <FeatureTypeStyle xmlns=\"http://www.opengis.net/se3d\">\n";
		sld3d+="		<Rule>\n";
		sld3d+="			<PolygonSymbolizer>\n";
		sld3d+="				<Fill>\n";
		sld3d+="					<Material>\n";
		sld3d+="						<DiffuseColor><SvgParameter xmlns=\"http://www.opengis.net/se\" name=\"\">#5f0000</SvgParameter></DiffuseColor>\n";
		sld3d+="						<SpecularColor><SvgParameter xmlns=\"http://www.opengis.net/se\" name=\"\">#6f6f6f</SvgParameter></SpecularColor>\n";
		sld3d+="						<AmbientColor><SvgParameter xmlns=\"http://www.opengis.net/se\" name=\"\">#111111</SvgParameter></AmbientColor>\n";
		sld3d+="						<Shininess>1</Shininess>\n";
		sld3d+="					</Material>\n";
		sld3d+="				</Fill>\n";
		sld3d+="			</PolygonSymbolizer>\n";
		sld3d+="		</Rule>\n";
		sld3d+="	 </FeatureTypeStyle>\n";
		sld3d+="	</sld3d:UserStyle>\n";
		sld3d+=" </sld3d:UserLayer>\n";
		sld3d+="</sld3d:StyledLayerDescriptor>\n";
		sld3d+="<sld3d:SRS>"+responseSRS+"</sld3d:SRS>\n";
		sld3d+="<sld3d:BBOX>\n";
		sld3d+="	<LowerCorner xmlns=\"http://www.opengis.net/ows\">"+dXLower+" "+dYLower+"</LowerCorner>\n";
		sld3d+="	<UpperCorner xmlns=\"http://www.opengis.net/ows\">"+dXUpper+" "+dYUpper+"</UpperCorner>\n";
		sld3d+="</sld3d:BBOX>\n";
		sld3d+=" <sld3d:POI>\n";
		sld3d+="	<gml:Point>\n";
		sld3d+="		<gml:coord>\n";
		sld3d+= addLinePoint(responseSRS, featureCollection, featureCollectionSRS);
		sld3d+="		</gml:coord>\n";
		sld3d+="	</gml:Point>\n";
		sld3d+=" </sld3d:POI>\n";
		sld3d+="<sld3d:LAYERS>\n";
		sld3d+="	<sld3d:LayerName>Buildings</sld3d:LayerName>\n";
		sld3d+="	<sld3d:LayerName>Terrain</sld3d:LayerName>\n";
		sld3d+="</sld3d:LAYERS>\n";
		sld3d+="<sld3d:FORMAT>model/vrml</sld3d:FORMAT>\n";
		sld3d+="</sld3d:GetScene>\n";
		
		try{
			if(filePath != null && fileName != null){
				String file = filePath+"/"+fileName;
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(sld3d.getBytes());
				fos.close();
			}
		}catch(IOException ioe){
			mLogger.error(ioe);
			ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addError(ErrorCodeType.UNKNOWN, "SLD3D", "IO-Problem! Message: "+se.getMessages());
            throw se;
		}
		
		return sld3d;
	}
	
	private static String addLinePoints(String responseSRS, FeatureCollection featureCollection, String featureCollectionSRS)throws ServiceError{
		String text = "";
		List featureList = featureCollection.getFeatures();
		
		Feature firstfeat = (Feature) featureList.get(0);
		LineString firstlineTMP = (LineString) firstfeat.getGeometry();
		Coordinate cFirst[] = firstlineTMP.getCoordinates();
		if(!featureCollectionSRS.equals(responseSRS)){
			Coordinate cTMP = CoordTransform.transformGetCoord(featureCollectionSRS, responseSRS, cFirst[0]);
			text+="<gml:pos>"+cTMP.x+" "+cTMP.y+" "+cTMP.z+"</gml:pos>\n";
		}else
			text+="<gml:pos>"+cFirst[0].x+" "+cFirst[0].y+" "+cFirst[0].z+"</gml:pos>\n";

		for (int i = 0; i < featureList.size(); i++) {
			Feature feat2 = (Feature) featureList.get(i);
			LineString lineTMP = (LineString) feat2.getGeometry();
			Coordinate c[] = lineTMP.getCoordinates();

			for (int j = 1; j < c.length; j++) {
				if(!featureCollectionSRS.equals(responseSRS)){
					Coordinate cTMP = CoordTransform.transformGetCoord(featureCollectionSRS, responseSRS, c[j]);
					text+="<gml:pos>"+cTMP.x+" "+cTMP.y+" "+cTMP.z+"</gml:pos>\n";
				}else
					text+="<gml:pos>"+c[j].x+" "+c[j].y+" "+c[j].z+"</gml:pos>\n";
			}
		}

		return text;
	}
	
	private static String addLinePoint(String responseSRS, FeatureCollection featureCollection, String featureCollectionSRS)throws ServiceError{
		String text = "";
		List featureList = featureCollection.getFeatures();
		
		Feature firstfeat = (Feature) featureList.get(0);
		LineString firstlineTMP = (LineString) firstfeat.getGeometry();
		Coordinate cFirst[] = firstlineTMP.getCoordinates();
		if(!featureCollectionSRS.equals(responseSRS)){
			Coordinate cTMP = CoordTransform.transformGetCoord(featureCollectionSRS, responseSRS, cFirst[0]);
			text+="<gml:pos>"+cTMP.x+" "+cTMP.y+" "+cTMP.z+"</gml:pos>\n";
		}else
			text+="<gml:pos>"+cFirst[0].x+" "+cFirst[0].y+" "+cFirst[0].z+"</gml:pos>\n";

		return text;
	}
}
