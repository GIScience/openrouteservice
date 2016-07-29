package org.freeopenls.routeservice.isochrones.isolinebuilders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.IndexedFeatureCollection;

import surface.Edge;
import surface.Node;
import surface.TIN;
import surface.Triangle;

import javax.vecmath.Point3d;

public class TINIsolineBuilder implements AbstractIsolineBuilder {
	
	private Point3d[] mArrayPoints3D;
	
	public TINIsolineBuilder(List<Coordinate> points, ZFunc timeFunc)
	{
		mArrayPoints3D = new Point3d[points.size()];
		
		for (int i = 0;i<points.size(); i++)
		{
			Coordinate c = points.get(i);
			mArrayPoints3D[i] = new Point3d(c.x, c.y, timeFunc.z(c));
		}
	}
	
	public Geometry computeIsoline(long level)
	{
		// *** Create TIN ***
		TIN tin = new TIN();
		tin.setVerbose(true);
		tin.snapping_distance = 0.000001;
		tin.triangulate(mArrayPoints3D);
		
		Vector<surface.Triangle> vecTriangles = tin.triangles;
		Vector<surface.Edge> vecEdges = tin.border_edges;
		
		/** Result ArrayList Polygon **/
		ArrayList<Geometry> arraylistPolygons = new ArrayList<Geometry>();
		/** FeatureCollection for create Polygon(s) for Accessibility Areas **/
		FeatureCollection featcoll;
		/** HashSet of FeatureIDs/LineIndex**/
		HashSet<Integer> hashsetFeatureIDs = new HashSet<Integer>();
		/** FeatureSchema of the FeatureCollection **/
		FeatureSchema featschema = new FeatureSchema();
		featschema.addAttribute("ID", AttributeType.INTEGER);
		featschema.addAttribute("hoehe", AttributeType.DOUBLE);
		featschema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
		
		featcoll = new FeatureDataset(featschema);
		/** GeometryFactory for create e.g. Polygon, LineString**/
		GeometryFactory gf = new GeometryFactory();
		int iLineIndex = 0;

		 ///////////////////////////////////////
		//Create Indexed-/FeatColl and HashSet to find the right the border_edge
		FeatureCollection featcollSurfaceEdges = new FeatureDataset(featschema);
		HashSet<Integer> hsSurfaceEdgeIndex = new HashSet<Integer>();
		for(int i=0 ; i<vecEdges.size() ; i++){
			Edge e = vecEdges.get(i);
			Node[] nArray = e.nodes;
			Coordinate[] c = new Coordinate[2];
			c[0] = new Coordinate(nArray[0].x, nArray[0].y, nArray[0].z);
			c[1] = new Coordinate(nArray[1].x, nArray[1].y, nArray[1].z);
			LineString ls = gf.createLineString(c);
			BasicFeature feat = new BasicFeature(featschema);
			feat.setGeometry(ls);
			feat.setAttribute("ID", i);
			featcollSurfaceEdges.add(feat);
			hsSurfaceEdgeIndex.add(i);
		}
		IndexedFeatureCollection indexedfeatcollSurfaceEdges = new IndexedFeatureCollection(featcollSurfaceEdges);


////TODO
////////*** KANN RAUSGENOMMEN WERDEN ***
//		FeatureCollection featOOOOOUT = new FeatureDataset(featschema);
//		HashSet<LineString> hsLSSpace = new HashSet<LineString>();
////////

		
		///////////////////////////////////////
		//*** Calculate Contour Line ***
		// -> for each triangle (if possible)!!
		for(int iIndex=0 ; iIndex<vecTriangles.size() ; iIndex++){
			ArrayList<Coordinate> newLineStringPoints = new ArrayList<Coordinate>();
			Triangle triangleTMP = vecTriangles.get(iIndex);
			Node[] nodeArrayTMP = triangleTMP.nodes;

////TODO
////////*** KANN RAUSGENOMMEN WERDEN ***
//			Edge edgesTMP[] = triangleTMP.edges;
//			for(int i=0 ; i<edgesTMP.length ; i++){
//				Node nTMP[] = edgesTMP[i].nodes;
//				
//				Coordinate c[] = new Coordinate[2];
//				c[0] = new Coordinate(nTMP[0].x, nTMP[0].y);
//				c[1] = new Coordinate(nTMP[1].x, nTMP[1].y);
//				LineString ls = gf.createLineString(c);
//				//if(!hsLSSpace.contains(ls)){
//					BasicFeature featTMP = new BasicFeature(featschema);
//					featTMP.setGeometry(ls);
//					featTMP.setAttribute("ID", iIndex);
//					featTMP.setAttribute("hoehe", Double.parseDouble(""+iIndex));
//					featOOOOOUT.add(featTMP);	
//				//}
//			}
////////
			
			//If all Nodes over or under the Contour Line
			if(nodeArrayTMP[0].z <= level && nodeArrayTMP[1].z <= level && nodeArrayTMP[2].z <= level
					|| nodeArrayTMP[0].z > level && nodeArrayTMP[1].z > level && nodeArrayTMP[2].z > level){

				// DO NOTHING !!
			}
			else{
				//		Node2
				//Node3
				//			Node1
				
				//Search lowest Node
				Node nodeLowest = nodeArrayTMP[0];
				for(int i=1 ; i<nodeArrayTMP.length ; i++){
					if(nodeArrayTMP[i].z < nodeLowest.z)
						nodeLowest = nodeArrayTMP[i];
				}
	
				//Search highest Node
				Node nodeHighest = nodeArrayTMP[0];
				for(int i=0 ; i<nodeArrayTMP.length ; i++){
					if(nodeArrayTMP[i].z >= nodeLowest.z && nodeArrayTMP[i].z >= nodeHighest.z && nodeArrayTMP[i] != nodeLowest)
						nodeHighest = nodeArrayTMP[i];
				}
				
				//Search middle Node
				Node nodeMiddle = nodeArrayTMP[0];
				for(int i=0 ; i<nodeArrayTMP.length ; i++){
					if(nodeArrayTMP[i].z >= nodeLowest.z && nodeArrayTMP[i] != nodeLowest && nodeArrayTMP[i] != nodeHighest)
						nodeMiddle = nodeArrayTMP[i];
				}

				//////////////////////////////////
				//*** Interpolation ***
				//	x1/h1 = x2/h2 
				double x1=0, x2=0, y1=0, y2=0, h1=0, h2=0;

				//////////////////////////////////
				//Node1-Node2
				if(nodeLowest.z <= level && level <= nodeMiddle.z){
					x2 = nodeMiddle.x - nodeLowest.x; h1 = level - nodeLowest.z;
					
					if(h1 == 0)
						newLineStringPoints.add(new Coordinate(nodeLowest.x, nodeLowest.y));
					else if(level == nodeMiddle.z)
						newLineStringPoints.add(new Coordinate(nodeMiddle.x, nodeMiddle.y));
					else{
						if(x2 == 0) x2=1;
						h2 = nodeMiddle.z - nodeLowest.z;
						if(h2 == 0) h2=1;
						x1 = ( h1 * x2 ) / h2;
						y2 = nodeMiddle.y - nodeLowest.y;
						if(y2 == 0) y2=1;
						y1 = ( x1 * y2 ) / x2;
						//Add Point 1-2
						newLineStringPoints.add(new Coordinate(nodeLowest.x + x1, nodeLowest.y + y1));
					}
				}
				//////////////////////////////////
				//Node2-Node3
				if(nodeMiddle.z <= level && level <= nodeHighest.z){
					x2 = nodeHighest.x - nodeMiddle.x; h1 = level - nodeMiddle.z;

					if(h1 == 0){
						if(!newLineStringPoints.contains(new Coordinate(nodeMiddle.x, nodeMiddle.y)))
							newLineStringPoints.add(new Coordinate(nodeMiddle.x, nodeMiddle.y));
					}
					else if(level == nodeHighest.z)
						newLineStringPoints.add(new Coordinate(nodeHighest.x, nodeHighest.y));
					else{
						if(x2 == 0) x2=1;
						h2 = nodeHighest.z - nodeMiddle.z;
						if(h2 == 0) h2=1;
						x1 = ( h1 * x2 ) / h2;
						y2 = nodeHighest.y - nodeMiddle.y;
						if(y2 == 0) y2=1;
						y1 = ( x1 * y2 ) / x2;
						//Add Point 2-3
						newLineStringPoints.add(new Coordinate(nodeMiddle.x + x1, nodeMiddle.y + y1));
					}			
				}
				//////////////////////////////////
				//Node3-Node1
				if(nodeLowest.z <= level && level <= nodeHighest.z){
					x2 = nodeHighest.x - nodeLowest.x; h1 = level - nodeLowest.z;
					
					if(h1 == 0){
						if(!newLineStringPoints.contains(new Coordinate(nodeLowest.x, nodeLowest.y)))
							newLineStringPoints.add(new Coordinate(nodeLowest.x, nodeLowest.y));
					}
					else if(level == nodeHighest.z && !newLineStringPoints.contains(new Coordinate(nodeHighest.x, nodeHighest.y)))
						newLineStringPoints.add(new Coordinate(nodeHighest.x, nodeHighest.y));
					else{
						if(x2 == 0) x2=1;
						h2 = nodeHighest.z - nodeLowest.z;
						if(h2 == 0) h2=1;
						x1 = ( h1 * x2 ) / h2;
						y2 = nodeHighest.y - nodeLowest.y;
						if(y2 == 0) y2=1;
						y1 = ( x1 * y2 ) / x2;
						//Add Point 3-1
						newLineStringPoints.add(new Coordinate(nodeLowest.x + x1, nodeLowest.y + y1));
					}
				}
			}

			//////////////////////////////////
			//*** Create LineString from the Points of the Contour Line ***
			if(newLineStringPoints.size() < 2){
			}
			else{
				//Create LineString
				Coordinate[] cArrayLineString = new Coordinate[newLineStringPoints.size()];
				for(int ii=0 ; ii<newLineStringPoints.size() ; ii++){
					cArrayLineString[ii] = newLineStringPoints.get(ii);
				}
				LineString lsTMP = gf.createLineString(cArrayLineString);
				
				//Add it to the FeatureCollection/HashSet to connect each LineString later 
				BasicFeature featNew = new BasicFeature(featschema);
				featNew.setGeometry(lsTMP);
				featNew.setAttribute("ID", iLineIndex);
				iLineIndex++;
				featcoll.add(featNew);
				hashsetFeatureIDs.add(iLineIndex);
			}
		}

////TODO		
////////*** For Check-Output ***
//		List list = featcoll.getFeatures();
//		FeatureCollection featOOOUT = new FeatureDataset(featschema);
//		for(int i=0 ; i<featcoll.size() ; i++){
//			Feature featTMP = (Feature)list.get(i);
//			featTMP.setAttribute("ID", i);
//			featOOOUT.add(featTMP);			
//		}
////////

		///////////////////////////////////////
		//*** Connect LineStrings for Contour Line ***
		IndexedFeatureCollection indexedfeatcoll = new IndexedFeatureCollection(featcoll);
		ArrayList<Polygon> arraylistPolygon = new ArrayList<Polygon>();

		while(featcoll.size() > 0){
			ArrayList<Coordinate> arraylistLSCoords = new ArrayList<Coordinate>();
			List listfeat = featcoll.getFeatures();
			
			Feature feat = (Feature) listfeat.get(0);
			Object objTMP = feat.getGeometry();
			int iID = Integer.parseInt(feat.getString("ID"));

			LineString lsTMP;
			
			if (objTMP instanceof MultiLineString)
				lsTMP = (LineString) ((MultiLineString) objTMP).getGeometryN(0);
			else
				lsTMP = (LineString) objTMP;

			Coordinate cEndTMP = lsTMP.getCoordinateN(1);
			arraylistLSCoords.add(lsTMP.getCoordinateN(0));
			arraylistLSCoords.add(lsTMP.getCoordinateN(1));
			featcoll.remove(feat);
			hashsetFeatureIDs.remove(iID);
			boolean boolNewPoint = false;

			do{
				boolNewPoint = false;
				Envelope env = new Envelope(cEndTMP);

				//Find next LineString
				List listQuery = indexedfeatcoll.query(env);
				for(int j=0 ; j<listQuery.size() ; j++){
					Feature featQuery = (Feature) listQuery.get(j);
					Object objQueryTMP = featQuery.getGeometry();
					int iIDQuery = Integer.parseInt(featQuery.getString("ID"));

					if(iIDQuery == iID){
						//Is it the same ID? -> Do Nothing!
					}
					else{
						LineString lsQueryTMP;
						if (objQueryTMP instanceof MultiLineString)
							lsQueryTMP = (LineString) ((MultiLineString) objQueryTMP).getGeometryN(0);
						else
							lsQueryTMP = (LineString) objQueryTMP;

						//Startpoint of the LineString
						Coordinate cTMP0 = lsQueryTMP.getCoordinateN(0);
						//Endpoint of the LineString
						Coordinate cTMP1 = lsQueryTMP.getCoordinateN(1);

						//Is it the Startpoint?
						if(cTMP0.equals(cEndTMP) && hashsetFeatureIDs.contains(iIDQuery)){
							arraylistLSCoords.add(cTMP1);
							cEndTMP = cTMP1;
							boolNewPoint = true;
							featcoll.remove(featQuery);
							hashsetFeatureIDs.remove(iIDQuery);
						}
						//Is it the Endpoint?
						else if(cTMP1.equals(cEndTMP) && hashsetFeatureIDs.contains(iIDQuery)){
							arraylistLSCoords.add(cTMP0);
							cEndTMP = cTMP0;
							boolNewPoint = true;
							featcoll.remove(featQuery);
							hashsetFeatureIDs.remove(iIDQuery);
						}
					}
				}

				//If no Point found, search on border_edges
				if(boolNewPoint == false){
					Envelope envBorder = new Envelope(cEndTMP);
					List listBorder = indexedfeatcollSurfaceEdges.query(envBorder);

					for(int iIndex=0 ; iIndex<listBorder.size() ; iIndex++){
						Feature featQueryBorder = (Feature)listBorder.get(iIndex);
						int iIDQueryBorder = Integer.parseInt(featQueryBorder.getString("ID"));

						if(hsSurfaceEdgeIndex.contains(iIDQueryBorder)){
							Object objQueryBorder = featQueryBorder.getGeometry();

							LineString lsQueryBorder;
							if (objQueryBorder instanceof MultiLineString) lsQueryBorder = (LineString) ((MultiLineString) objQueryBorder).getGeometryN(0);
							else lsQueryBorder = (LineString) objQueryBorder;

							Coordinate cTMP1Border = lsQueryBorder.getCoordinateN(0);
							Coordinate cTMP2Border = lsQueryBorder.getCoordinateN(1);

							//Is it the Startpoint?
							if(cTMP1Border.equals(cEndTMP) && hsSurfaceEdgeIndex.contains(iIDQueryBorder) 
									&& level >= cTMP2Border.z && !arraylistLSCoords.contains(cTMP2Border)){
								arraylistLSCoords.add(cTMP2Border);
								cEndTMP = cTMP2Border;
								boolNewPoint = true;
								hsSurfaceEdgeIndex.remove(iIDQueryBorder);
							}
							//Is it the Endpoint?
							else if(cTMP2Border.equals(cEndTMP) && hsSurfaceEdgeIndex.contains(iIDQueryBorder) 
									&& level >= cTMP1Border.z && !arraylistLSCoords.contains(cTMP1Border)){
								arraylistLSCoords.add(cTMP1Border);
								cEndTMP = cTMP1Border;
								boolNewPoint = true;
								hsSurfaceEdgeIndex.remove(iIDQueryBorder);
							}
						}
					}
				}
				
				//If no Point found, search on border_edges and interpolate!!!!!!!
				if(boolNewPoint == false){
					Envelope envBorder = new Envelope(cEndTMP);
					List listBorder = indexedfeatcollSurfaceEdges.query(envBorder);

					for(int iIndex=0 ; iIndex<listBorder.size() ; iIndex++){
						Feature featQueryBorder = (Feature)listBorder.get(iIndex);
						int iIDQueryBorder = Integer.parseInt(featQueryBorder.getString("ID"));

						if(hsSurfaceEdgeIndex.contains(iIDQueryBorder)){
							Object objQueryBorder = featQueryBorder.getGeometry();

							LineString lsQueryBorder;
							if (objQueryBorder instanceof MultiLineString) lsQueryBorder = (LineString) ((MultiLineString) objQueryBorder).getGeometryN(0);
							else lsQueryBorder = (LineString) objQueryBorder;

							Coordinate cTMP1Border = lsQueryBorder.getCoordinateN(0);
							Coordinate cTMP2Border = lsQueryBorder.getCoordinateN(1);

							if((level >= cTMP1Border.z && level <= cTMP2Border.z) || (level >= cTMP2Border.z && level <= cTMP1Border.z)
									&& hsSurfaceEdgeIndex.contains(iIDQueryBorder)){

								if(cTMP1Border.z <= level && !arraylistLSCoords.contains(cTMP1Border)){
									arraylistLSCoords.add(cTMP1Border);
									cEndTMP = cTMP1Border;
									boolNewPoint = true;
									hsSurfaceEdgeIndex.remove(iIDQueryBorder);
								}else if(cTMP2Border.z <= level && !arraylistLSCoords.contains(cTMP2Border)){
									arraylistLSCoords.add(cTMP2Border);
									cEndTMP = cTMP2Border;
									boolNewPoint = true;
									hsSurfaceEdgeIndex.remove(iIDQueryBorder);
								}else{
									if(cTMP1Border.z > cTMP2Border.z){
										Coordinate cTMP = cTMP1Border; cTMP1Border = cTMP2Border; cTMP2Border = cTMP;
									}

									double x2 = cTMP2Border.x - cTMP1Border.x; double h1 = level - cTMP1Border.z;
									if(h1 == 0){
										arraylistLSCoords.add(cTMP1Border);
										cEndTMP = cTMP1Border;
										boolNewPoint = true;
										hsSurfaceEdgeIndex.remove(iIDQueryBorder);
									}
									else{
										if(x2 == 0) x2=1;
										double h2 = cTMP2Border.z - cTMP1Border.z;
										if(h2 == 0) h2=1;
										double x1 = ( h1 * x2 ) / h2;
										double y2 = cTMP2Border.y - cTMP1Border.y;
										if(y2 == 0) y2=1;
										double y1 = ( x1 * y2 ) / x2;
										
										cEndTMP = new Coordinate(cTMP1Border.x + x1, cTMP1Border.y + y1);
										arraylistLSCoords.add(cEndTMP);
										boolNewPoint = true;
										hsSurfaceEdgeIndex.remove(iIDQueryBorder);
									}
								}
							}
						}
					}
				}
				
			}while(boolNewPoint == true);

			//Check Number
			if(arraylistLSCoords.size() > 2){	
				arraylistLSCoords.add(arraylistLSCoords.get(0));
				Coordinate[] coordsTMP = new Coordinate[arraylistLSCoords.size()];
	
				for(int iIndexCoords=0 ; iIndexCoords<arraylistLSCoords.size() ; iIndexCoords++){
					coordsTMP[iIndexCoords] = arraylistLSCoords.get(iIndexCoords);
				}

				LinearRing lrTMP = gf.createLinearRing(coordsTMP);
				arraylistPolygon.add(gf.createPolygon(lrTMP, null));
			}
		}


		///////////////////////////////////////
		//*** Sort Result ***
		while(arraylistPolygon.size() > 0){
			//Find biggest Polygon
			Polygon polyBIG = arraylistPolygon.get(0);
			for(int i = 1 ; i<arraylistPolygon.size() ; i++) {
				Polygon polyTMP = arraylistPolygon.get(i);
				
				if(polyTMP.getArea() > polyBIG.getArea()){
					polyBIG = polyTMP;
				}
			}
			arraylistPolygon.remove(polyBIG);
		//	calulateOverAllEnvelope(polyBIG.getEnvelopeInternal());

//TODO - muss ueberarbeitet werden!!!
//System.out.println(" "+polyBIG.toText());
//			for(int i=0 ; i<arraylistPolygon.size() ; i++){
//				System.out.println("  "+arraylistPolygon.get(i).toText());
//				if(polyBIG.contains(arraylistPolygon.get(i))){
//					System.out.println("  here");
//					polyBIG = (Polygon) polyBIG.difference(arraylistPolygon.get(i));
//					arraylistPolygon.remove(arraylistPolygon.get(i));
//				}
//			}
			arraylistPolygons.add(polyBIG);

		}

		return gf.createGeometryCollection(arraylistPolygons.toArray(new Geometry[arraylistPolygons.size()]));
	}
}
