

package org.freeopenls.location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.freeopenls.constants.RouteService;
import org.freeopenls.error.ServiceError;
import org.freeopenls.gml.ArcByCenterPoint;
import org.freeopenls.gml.Envelope;
import org.freeopenls.overlay.Overlay;
import org.freeopenls.routeservice.routing.AvoidFeatureFlags;
import org.freeopenls.tools.CoordTools;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.IndexedFeatureCollection;

import net.opengis.gml.CircleByCenterPointType;
import net.opengis.gml.EnvelopeType;
import net.opengis.gml.PolygonType;
import net.opengis.xls.AbstractLocationType;
import net.opengis.xls.AddressType;
import net.opengis.xls.AreaOfInterestType;
import net.opengis.xls.AvoidFeatureType;
import net.opengis.xls.AvoidListType;
import net.opengis.xls.PointOfInterestType;
import net.opengis.xls.PositionType;

/**
 * Class for read the xls:AvoidList Element
 * 
 * Copyright: Copyright (c) 2008 by Pascal Neis
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * @version 1.0 2006-10-30
 * @version 1.1 2008-04-23
 */
public class AvoidList {
	/** ArrayList<Polygon> AreasOfInterest (Areas to avoid) */
	private ArrayList<Polygon> m_AvoidAreas = new ArrayList<Polygon>();
	/** ArrayList<Coordinate> Locations (Locations to avoid) */
	private ArrayList<Integer> m_iLocationsEdgeIDList = new ArrayList<Integer>();
	/** ArrayList<LineString> LineStrings (Geometry) of Address to avoid */
	private ArrayList<LineString> m_AvoidLineStrings = new ArrayList<LineString>();
	/** List of Edge IDs to avoid */ 
	private HashSet<Integer> mAvoidEdges = new HashSet<Integer>();
	/** FeatureCollection for Overlay with AvoidPolygons */
	private FeatureCollection mFeatCollAvoidPolygons;

	/**
	 * Constructor - Defines the list of areas, locations and types of features <br>
	 * in which the route should avoid passing through.
	 * 
	 * @param indexfeatcoll
	 * @param avoidlistType
	 * @param pointRadiusEdgeStartValue
	 * @param pointRadiusEdgeMaxValue
	 * @param edgeIDAvoidFeatureHighway
	 * @param edgeIDAvoidFeatureTollway
	 * @param openLSLocationUtilityServicePath
	 * @param openLSDirectoryServicePath
	 * @throws ServiceError
	 * @throws Exception
	 */
	public AvoidList(AvoidListType avoidlistType, 
			double pointRadiusEdgeMaxValue,
			HashSet<Integer> edgeIDAvoidFeatureHighway, HashSet<Integer> edgeIDAvoidFeatureTollway,
			String openLSLocationUtilityServicePath, String openLSDirectoryServicePath)throws ServiceError, Exception{

		////////////////////////////////////////////////////////
		//Areas to avoid
		//minOccurs="0" maxOccurs="unbounded"
		AreaOfInterestType aoiArray[] = avoidlistType.getAOIArray();	
		if(aoiArray.length > 0){
			for(int i=0 ; i < aoiArray.length ; i++){
				
				//Arc/CircleByCenterPoint
				if(aoiArray[i].isSetCircleByCenterPoint()){
					CircleByCenterPointType circlePoint = aoiArray[i].getCircleByCenterPoint();
					ArcByCenterPoint arc = new ArcByCenterPoint(RouteService.GRAPH_SRS, circlePoint);
					m_AvoidAreas.add(arc.createPolygon());
				}
				
				//Polygon
				if(aoiArray[i].isSetPolygon()){
					PolygonType polygonType = aoiArray[i].getPolygon();
					org.freeopenls.gml.Polygon poly = new org.freeopenls.gml.Polygon(RouteService.GRAPH_SRS, polygonType);
					m_AvoidAreas.add(poly.getPolygon());
				}

				//Envelope
				if(aoiArray[i].isSetEnvelope()){
					EnvelopeType envelopeType = aoiArray[i].getEnvelope();
					Envelope env = new Envelope(RouteService.GRAPH_SRS, envelopeType);
					m_AvoidAreas.add(env.createPolygon());
				}
			}
		}

		////////////////////////////////////////////////////////
		//Locations/Points to avoid
		//minOccurs="0" maxOccurs="unbounded"
		AbstractLocationType alType[] = avoidlistType.getLocationArray();	
		if(alType.length > 0){
			for(int i=0 ; i < alType.length ; i++){
				
				//Position
				if (alType[i]  instanceof PositionType){
					Position pos = new Position();
					pos.setPosition(RouteService.GRAPH_SRS, (PositionType) alType[i].changeType(PositionType.type));
					Coordinate cTMP = pos.getPosition().getCoordinate();
					//Get all Edges in whom BBoxes the Position with a BBOX of .... is
					List lTMP = getEdgeID(cTMP, pointRadiusEdgeMaxValue);

					if(lTMP.size() > 0 && lTMP != null){
						for(int j=0 ; j < lTMP.size() ; j++){
							Feature feat = (Feature) lTMP.get(j);
							double dDistance = CoordTools.findShortestDistanceToGeometry(feat.getGeometry(), cTMP, pointRadiusEdgeMaxValue);
						  	//Add only Edges to the list which are in a distance of ... to the Position 
							if(dDistance < pointRadiusEdgeMaxValue){
								m_iLocationsEdgeIDList.add((Integer)feat.getAttribute("EdgeID"));
								
								Object obj = feat.getGeometry();
								LineString ls = null;
							  	if (obj instanceof MultiLineString)
							  		ls = (LineString) ((MultiLineString)obj).getGeometryN(0);
							  	else
							  		ls = (LineString)obj;
								m_AvoidLineStrings.add(ls);
							}
						}
					}					
				}
				
				//PointOfInterest (POI)
				if(alType[i] instanceof PointOfInterestType){
					PointOfInterest poi = new PointOfInterest(openLSLocationUtilityServicePath, openLSDirectoryServicePath);
					poi.setPointOfInterest(RouteService.GRAPH_SRS, (PointOfInterestType) alType[i].changeType(PointOfInterestType.type));
					Coordinate cTMP = poi.getPosition().getCoordinate();
					
					//Get all Edges in whom BBoxes the POI with a BBOX of .... is
					List lTMP = getEdgeID(cTMP, pointRadiusEdgeMaxValue);
					
					if(lTMP.size() > 0 && lTMP != null){
						for(int j=0 ; j < lTMP.size() ; j++){
							Feature feat = (Feature) lTMP.get(j);
							double dDistance = CoordTools.findShortestDistanceToGeometry(feat.getGeometry(), cTMP, 50);
							//Add only Edges to the list which are in a distance of ... to the POI
							if(dDistance < pointRadiusEdgeMaxValue){
								m_iLocationsEdgeIDList.add((Integer)feat.getAttribute("EdgeID"));
								
								Object obj = feat.getGeometry();
								LineString ls = null;
							  	if (obj instanceof MultiLineString)
							  		ls = (LineString) ((MultiLineString)obj).getGeometryN(0);
							  	else
							  		ls = (LineString)obj;
								m_AvoidLineStrings.add(ls);
							}
						}
					}
				}
				
				//Address
				if(alType[i] instanceof AddressType){
					GeocodeAddress address = new GeocodeAddress(openLSLocationUtilityServicePath);
					Coordinate cTMP = address.geocode((AddressType) alType[i].changeType(AddressType.type)).getCoordinate();
					if(cTMP != null){
						List lTMP = getEdgeID(cTMP, pointRadiusEdgeMaxValue);
						if(lTMP.size() > 0 && lTMP != null){
							for(int j=0 ; j < lTMP.size() ; j++){
								Feature feat = (Feature) lTMP.get(j);
								double dDistance = CoordTools.findShortestDistanceToGeometry(feat.getGeometry(), cTMP, pointRadiusEdgeMaxValue);
								//Add only Edges to the list which are in a distance of ... to the Address
								if(dDistance < pointRadiusEdgeMaxValue){
									m_iLocationsEdgeIDList.add((Integer)feat.getAttribute("EdgeID"));
									
									Object obj = feat.getGeometry();
									LineString ls = null;
								  	if (obj instanceof MultiLineString)
								  		ls = (LineString) ((MultiLineString)obj).getGeometryN(0);
								  	else
								  		ls = (LineString)obj;
									m_AvoidLineStrings.add(ls);
								}
							}
						}
					}
				}
			}
		}

		////////////////////////////////////////////////////////
		//AvoidFeature "Highway" and/or "Tollway"
		//minOccurs="0" maxOccurs="unbounded"
		AvoidFeatureType.Enum avoidfeatureType[] = avoidlistType.getAvoidFeatureArray();
		if(avoidfeatureType.length > 0){
			for(int i=0 ; i < avoidfeatureType.length ; i++){
				String sTMP = avoidfeatureType[i].toString();
				if(sTMP.equals(AvoidFeatureType.TOLLWAY.toString()))
					mAvoidEdges.addAll(edgeIDAvoidFeatureTollway);
				if(sTMP.equals(AvoidFeatureType.HIGHWAY.toString()))
					mAvoidEdges.addAll(edgeIDAvoidFeatureHighway);
			}
		}
		
		
		//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		
		//AvoidLocation - Addresses, Points ...
		if(m_iLocationsEdgeIDList.size() > 0){
			mAvoidEdges.addAll(m_iLocationsEdgeIDList);
		}
		//AvoidAreas - Polygon, Circle ...
		if(m_AvoidAreas.size() > 0){
			//FeatureCollection of AvoidPolygons
			FeatureSchema featsch = new FeatureSchema();
			featsch.addAttribute("ID", AttributeType.INTEGER);
			featsch.addAttribute("Geometry", AttributeType.GEOMETRY);
			mFeatCollAvoidPolygons = new FeatureDataset(featsch);

			for(int i=0 ; i < m_AvoidAreas.size() ; i++){
				Feature feat = new BasicFeature(featsch);
				feat.setAttribute("ID", i);
				feat.setAttribute("Geometry", m_AvoidAreas.get(i));
				mFeatCollAvoidPolygons.add(feat);
			}
/*
			//Overlay Graph <> AvoidPolygons
			PGConnection conn = GraphManager.getInstance().getConnectionManager().getFreeConnection();
			try{
				org.freeopenls.database.postgis.operations.Overlay overlay = 
					new org.freeopenls.database.postgis.operations.Overlay(
							GraphManager.getInstance().getConnectionManager().getConnParamterDB(), conn);
				mAvoidEdges.addAll(overlay.doOverlay(m_AvoidAreas));
			}finally{
				GraphManager.getInstance().getConnectionManager().enableConnection(conn.getConnectionNumber());
			}*/
		}
	}
	
	public static int getAvoidFeatureTypes(AvoidListType avoidListType)
	{
	    AvoidFeatureType.Enum[] types = avoidListType.getAvoidFeatureArray();
		
		if (types != null && types.length > 0)
		{
			int flags = 0;
			
			for (int i = 0; i < types.length; i++)
			{
				AvoidFeatureType.Enum aft = types[i];
				if (aft == AvoidFeatureType.HIGHWAY)
					flags |= AvoidFeatureFlags.Highway;
				else if (aft == AvoidFeatureType.TOLLWAY)
					flags |= AvoidFeatureFlags.Tollway;
				else if (aft == AvoidFeatureType.FERRY)
					flags |= AvoidFeatureFlags.Ferries;
				else if (aft == AvoidFeatureType.UNPAVEDROADS)
					flags |= AvoidFeatureFlags.UnpavedRoads;
				else if (aft == AvoidFeatureType.PAVEDROADS)
					flags |= AvoidFeatureFlags.PavedRoads;
				else if (aft == AvoidFeatureType.TRACKS)
					flags |= AvoidFeatureFlags.Tracks;
				else if (aft == AvoidFeatureType.STEPS)
					flags |= AvoidFeatureFlags.Steps;
				else if (aft == AvoidFeatureType.BRIDGES)
					flags |= AvoidFeatureFlags.Bridges;
				else if (aft == AvoidFeatureType.TUNNELS)
					flags |= AvoidFeatureFlags.Tunnels;
				else if (aft == AvoidFeatureType.BORDERS)
					flags |= AvoidFeatureFlags.Borders;
				else if (aft == AvoidFeatureType.FORDS)
					flags |= AvoidFeatureFlags.Fords;
				else if (aft == AvoidFeatureType.HILLS)
					flags |= AvoidFeatureFlags.Hills;
			}
			
			return flags;
		}
		
		return 0;
	}
	
	public static ArrayList<Polygon> getAvoidPolygons(AvoidListType avoidlistType) throws ServiceError
	{
		ArrayList<Polygon> avoidAreas = new ArrayList<Polygon>();
		
		AreaOfInterestType aoiArray[] = avoidlistType.getAOIArray();	
		if(aoiArray.length > 0){
			for(int i=0 ; i < aoiArray.length ; i++){
				
				//Arc/CircleByCenterPoint
				if(aoiArray[i].isSetCircleByCenterPoint()){
					CircleByCenterPointType circlePoint = aoiArray[i].getCircleByCenterPoint();
					ArcByCenterPoint arc = new ArcByCenterPoint(RouteService.GRAPH_SRS, circlePoint);
					avoidAreas.add(arc.createPolygon());
				}
				
				//Polygon
				if(aoiArray[i].isSetPolygon()){
					PolygonType polygonType = aoiArray[i].getPolygon();
					org.freeopenls.gml.Polygon poly = new org.freeopenls.gml.Polygon(RouteService.GRAPH_SRS, polygonType);
					avoidAreas.add(poly.getPolygon());
				}

				//Envelope
				if(aoiArray[i].isSetEnvelope()){
					EnvelopeType envelopeType = aoiArray[i].getEnvelope();
					Envelope env = new Envelope(RouteService.GRAPH_SRS, envelopeType);
					avoidAreas.add(env.createPolygon());
				}
			}
		}
		
		return avoidAreas;
	}

	/**
	 * Method that returns a List with EdgeIDs which find about IndexedFaetureCollection<br>
	 * with an envelope and radius.
	 * 
	 * @param c Coordinate Point to creating envelope
	 * @param dEnvelopeRadiusLength double value to creating envelope
	 * @return List
	 * @throws ServiceError
	 */
	private List getEdgeID(Coordinate c, double dEnvelopeMaxValue)throws ServiceError{
		
		List list = null;
		/*	PGConnection conn = GraphManager.getInstance().getConnectionManager().getFreeConnection();
		try{
			SearchNextLineString next = new SearchNextLineString(
					GraphManager.getInstance().getConnectionManager().getConnParamterDB(), conn);
			
			list = next.search(c, dEnvelopeMaxValue);
			
		}finally{
			GraphManager.getInstance().getConnectionManager().enableConnection(conn.getConnectionNumber());
		}*/
		
		return list;
	}

	/**
	 * Method that returns the ArrayList<Polygon> with AvoidAreas
	 * 
	 * @return ArrayList<Polygon>
	 */
	public ArrayList<Polygon> getAvoidAreas(){
		return m_AvoidAreas;
	}

	/**
	 * Method that returns the ArrayList<LineString> with AvoidAddresses
	 * 
	 * @return ArrayList<LineString>
	 */
	public ArrayList<LineString> getAvoidLines(){
		return m_AvoidLineStrings;
	}

	/**
	 * @return the AvoidEdges
	 */
	public HashSet<Integer> getAvoidEdges() {
		return mAvoidEdges;
	}
}
