/**
 * This library provides method to compute Concave Hull of a set of points
 * <p>
 * Currently three criteria are provided:
 * <p>
 * Alpha shape in Edelsbrunner, Herbert; Kirkpatrick, David G.; Seidel, Raimund (1983), "On the shape of a set of points in the plane", IEEE Transactions on Information Theory, 29 (4): 551�559, doi:10.1109/TIT.1983.1056714
 * <p>
 * Chi criterion in Matt Duckham et al 2008 Efficient generation of simple polygons for characterizing the shape of a set of points in the plane
 * <p>
 * Park edge ratio criterion in JIN-SEO PARK AND SE-JONG OH (2012) A New Concave Hull Algorithm and Concaveness Measure for n-dimensional Datasets, JOURNAL OF INFORMATION SCIENCE AND ENGINEERING 28, 587-600
 * <p>
 * Alpha shape is effectively to use a disc of radius R = 1/alpha to remove space among points without enclosing any point in disc's interior
 * Therefore, if a triangle has a circumcircle with a radius large than the given R, the triangle may be removed from the initial hull. For an infinite R (alpha = 0), it becomes the convex hull of the point set
 * <p>
 * Chi-criterion is the edge length threshold. If an edge of a triangle is longer than the threshold, the triangle (in fact, often two) may be removed from the initial hull.
 * <p>
 * Park edge ratio is the ratio between the length of the "outter" edge and the shorter inner edge. Unlike the above two criteria, it is scale-independent.
 * <p>
 * In this library we provide several different implementations.
 * <p>
 * Criteria for triangle removal is parametrised and implemented as the TriangleChecker interface.
 * <p>
 * getConcaveHullDFS supports all three criteria. It starts the "digging" from the first qualified boundary edge and follows a depth-first strategy. The "dig" stops only if no more dig-able edges left inside a "cave", then it will start digging again with another qualified boundary edge.
 * <p>
 * getConcaveHullBFS support all three criteria with an option of whether allow multiple parts to be generated. It follows a breadth-first strategy and at each step it will "dig" the longest qualified edge.
 * <p>
 * getConcaveHullWithHolesAlpha supports alpha shape criterion and will multiple parts as well as generate holes if applicable.
 * <p>
 * getConcaveHullWithHolesChi supports Chi criterion and also generate multiple parts and holes if applicable.
 * <p>
 * <p>
 * This library is built entirely on top of JTS geometry and Delaunay triangulation libraries. Therefore, it will have the same numerical robustness issues as has JTS.
 * <p>
 * <p>
 * Author: Sheng Zhou (Sheng.Zhou@os.uk)
 * <p>
 * version 0.5
 * <p>
 * Date: 2019-04-24
 * <p>
 * Copyright (C) 2019 Ordnance Survey
 * <p>
 * Licensed under the Open Government Licence v3.0 (the "License");
 * <p>
 * you may not use this file except in compliance with the License.
 * <p>
 * You may obtain a copy of the License at
 * <p>
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.os_concavehull;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.densify.Densifier;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;
import com.vividsolutions.jts.triangulate.quadedge.QuadEdge;
import com.vividsolutions.jts.triangulate.quadedge.QuadEdgeSubdivision;
import com.vividsolutions.jts.triangulate.quadedge.Vertex;
import org.apache.log4j.Logger;

import java.util.*;

public class ConcaveHull {
	static final Logger LOGGER = Logger.getLogger(ConcaveHull.class.getName());

	Coordinate hullCoord = null; // a coordinate on the convex hull of the dataset, used as a seed coordinate for various operations
	LinkedList<Coordinate> hullDT = null; //initial hull of the dataset (closed, first==last), built from JTS DT. It is normally (but not always) the convex hull (due to JTS DT's use of a bounding super triangle)
	QuadEdgeSubdivision sd = null;
	GeometryFactory gf = null;
	Geometry convexHull = null;
	//

	/** constructor that takes a JTS geometry as input
	 * @param geom
	 */
	public ConcaveHull(Geometry geom) {
		Coordinate[] coordArray = geom.getCoordinates();
		initArray(coordArray, geom.getFactory());
	}

	/** constructor that takes a JTS geometry as input, with optional densification
	 * @param geom input geometry
	 * @param densifyTol tolerance for densification of linear geometry or boundary of areal geometry. 0.0 for none
	 */
	public ConcaveHull(Geometry geom, double densifyTol) {
		if (densifyTol > 0.0) {
			geom = Densifier.densify(geom, densifyTol);
		}
		Coordinate[] coordArray = geom.getCoordinates();
		initArray(coordArray, geom.getFactory());
	}

	/** constructor that takes a coordinate collection as input
	 * @param coordCol input JTS Coordinate collection
	 * @param gf GeometryFactory to be used for geometry construction
	 */
	public ConcaveHull(Collection<Coordinate> coordCol, GeometryFactory gf) {
		initCol(coordCol, gf);
	}

	/** constructor that takes a coordinate array as input
	 * @param coordArray
	 * @param gf
	 */
	public ConcaveHull(Coordinate[] coordArray, GeometryFactory gf) {
		initArray(coordArray, gf);
	}

	/** constructor that takes a Geometry collection as input, with densification
	 * @param geomCol
	 * @param densifyTol tolerance for densification of linear geometry or boundary of areal geometry. 0.0 for none
	 */
	public ConcaveHull(Collection<Geometry> geomCol, double densifyTol) {
		if (geomCol != null && geomCol.size() > 0) {
			Collection<Geometry> gc = null;
			if (densifyTol > 0.0) {
				gc = new Vector<Geometry>(geomCol.size());
				for (Geometry geom : geomCol) {
					gc.add(Densifier.densify(geom, densifyTol));
				}
			} else {
				gc = geomCol;
			}
			Collection<Coordinate> coordCol = geomCol2Coordinate(gc);
			initCol(coordCol, geomCol.iterator().next().getFactory());
		}
	}

	//
	private static Collection<Coordinate> geomCol2Coordinate(Collection<Geometry> geomCol) {
		Vector<Coordinate> coordCol = new Vector<Coordinate>();
		if (geomCol != null) {
			for (Geometry geom : geomCol) {
				Coordinate[] coords = geom.getCoordinates();
				for (Coordinate coord : coords) {
					coordCol.add(coord);
				}
			}
		}
		return coordCol;
	}

	private static Collection<Collection<Coordinate>> geomCol2CoordinatePartition(Collection<Geometry> geomCol, int partitionSize) {
		Vector<Collection<Coordinate>> coordCol = new Vector<Collection<Coordinate>>();
		if (geomCol != null) {
			Vector<Coordinate> partition = new Vector<Coordinate>();
			for (Geometry geom : geomCol) {
				Coordinate[] coords = geom.getCoordinates();
				for (Coordinate coord : coords) {
					partition.add(coord);
					if (partition.size() == partitionSize) {
						coordCol.add(partition);
						partition = new Vector<Coordinate>();
					}
				}
			}
		}
		return coordCol;
	}

	private static Collection<Collection<Coordinate>> coordinateColPartition(Collection<Coordinate> coords, int partitionSize) {
		Vector<Collection<Coordinate>> coordCol = new Vector<Collection<Coordinate>>();
		if (coords != null) {
			Vector<Coordinate> partition = new Vector<Coordinate>();
			for (Coordinate coord : coords) {
				partition.add(coord);
				if (partition.size() == partitionSize) {
					coordCol.add(partition);
					partition = new Vector<Coordinate>();
				}
			}
			if (partition.size() > 0) {
				coordCol.add(partition);
			}
		}
		return coordCol;
	}

	/** knowing coord is in DT, locate the quad-edge whose origin is at coord
	 * @param sd
	 * @param coord
	 * @return
	 */
	private static QuadEdge locateVertexInDT(QuadEdgeSubdivision sd, Coordinate coord) {
		QuadEdge qe = sd.locate(coord);
		if (qe == null) { // something wrong
			return null;
		}
		if (qe.orig().getCoordinate().equals2D(coord)) {
			return qe;
		} else if (qe.dest().getCoordinate().equals2D(coord)) {
			return qe.sym();
		} else {
			return null;
		}
	}

	/** get the hull from current DT, which is the CH of data (or almost the CH of data)
	 * @param sd
	 * @param baseCoord
	 * @return
	 */
	private static LinkedList<Coordinate> getHull(QuadEdgeSubdivision sd, Coordinate baseCoord) {
		//
		QuadEdge qe = locateVertexInDT(sd, baseCoord);
		assert qe != null;
		Vertex baseVer = qe.orig();
		// find a frame vertex
		while (!sd.isFrameVertex(qe.dest())) {
			qe = qe.oNext();
		}
		// turn to a hull edge
		while (sd.isFrameVertex(qe.dest())) {
			qe = qe.oNext();
		}
		LinkedList<Coordinate> hull = new LinkedList<>();
		do {
			Vertex ver = qe.orig();
			hull.add(ver.getCoordinate());
			qe = qe.rPrev();
			while (sd.isFrameVertex(qe.dest())) {
				qe = qe.oNext();
			}
		} while (qe.orig() != baseVer);
		hull.add(hull.getFirst()); // close the ring, now first == last
		return hull;
	}

	//
	private void initArray(Coordinate[] coordArray, GeometryFactory gf) {
		this.gf = gf;
		Collection<Coordinate> coordCol = new Vector<Coordinate>(coordArray.length);
		for (Coordinate coord : coordArray) {
			coordCol.add(coord);
		}
		init(coordCol, coordArray);
	}
	//

	private void initCol(Collection<Coordinate> coordCol, GeometryFactory gf) {
		this.gf = gf;
		Coordinate[] coordArray = new Coordinate[coordCol.size()];
		coordCol.toArray(coordArray);
		init(coordCol, coordArray);
	}

	private void init(Collection<Coordinate> coordCol, Coordinate[] coordArray) {
		ConvexHull ch = new ConvexHull(coordArray, gf);
		Geometry chGeom = ch.getConvexHull();
		convexHull = chGeom;
		coordArray = null;
		hullCoord = chGeom.getCoordinate();
		DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
		builder.setSites(coordCol);
		sd = builder.getSubdivision();
		hullDT = getHull(sd, hullCoord);
	}
	//

	/** generate a circular list edge representation of a hull in the form of a CLOSED coordinate list. the order of the coordinates should be CCW
	 * @param hullCoords
	 * @return
	 */
	private DLCirList<Coordinate> generateHullEdgeRep(LinkedList<Coordinate> hullCoords) {
		DLCirList<Coordinate> rtn = new DLCirList<Coordinate>();
		ListIterator<Coordinate> iter = hullCoords.listIterator(1); // 2nd in list (should have at least 3 coords)
		while (iter.hasNext()) {
			Coordinate coord = iter.next();
			rtn.add(coord);
		}
		return rtn;
	}
	//

	/**
	 * @param hullCL
	 * @param coordNodeMap
	 * @param edgeIdx
	 * @param nodeEdgeMap
	 */
	private void generateHullIndices(DLCirList<Coordinate> hullCL, Map<Coordinate, DLNode<Coordinate>> coordNodeMap, Set<HullEdgeCir> edgeIdx, Map<DLNode<Coordinate>, HullEdgeCir> nodeEdgeMap) {
		DLNode<Coordinate> node = hullCL.getNode();
		do {
			Coordinate coord = node.getObj();
			coordNodeMap.put(coord, node);
			HullEdgeCir e = new HullEdgeCir(node);
			edgeIdx.add(e);
			nodeEdgeMap.put(node, e);
			node = node.getNext();
		} while (node != hullCL.getNode());
	}

	private void generateHullIndices(DLCirList<Coordinate> hullCL, TriangleMetric m, Map<Coordinate, DLNode<Coordinate>> coordNodeMap, Set<HullEdgeCir> edgeIdx, Map<DLNode<Coordinate>, HullEdgeCir> nodeEdgeMap) {
		DLNode<Coordinate> node = hullCL.getNode();
		do {
			Coordinate coord = node.getObj();
			coordNodeMap.put(coord, node);
			HullEdgeCir e = new HullEdgeCir(node, m);
			edgeIdx.add(e);
			nodeEdgeMap.put(node, e);
			node = node.getNext();
		} while (node != hullCL.getNode());

	}

	public Geometry exportTriangles(GeometryFactory gf) {
		if (sd != null) {
			return sd.getTriangles(gf);
		} else {
			return null;
		}
	}

	/** dig depth-first, faster but may generate weird results
	 * @param triChecker Triangle Checker to be used
	 * @return concave hull of input data
	 */
	public Geometry getConcaveHullDFS(TriangleChecker triChecker) {
		if (triChecker != null) {
			TreeSet<Coordinate> nodeSet = new TreeSet<Coordinate>(hullDT);
			LinkedList<Coordinate> hullList = new LinkedList<Coordinate>(hullDT);
			boolean modified = true;
			while (modified) {
				modified = false;
				ListIterator<Coordinate> iter = hullList.listIterator(0);
				while (iter.hasNext()) {
					Coordinate nodeS = iter.next(); // iter after nodeS
					if (iter.hasNext()) {
						Coordinate nodeE = iter.next(); // iter afer nodeE
						QuadEdge qe = sd.locate(nodeS, nodeE);
						if (qe == null) {
							System.out.println("fail to find edge: " + nodeS.toString() + " -" + nodeE.toString());
							return null;
						}
						QuadEdge lnext = qe.lNext();
						Vertex verO = lnext.dest();
						Coordinate nodeO = verO.getCoordinate();
						if (nodeSet.contains(nodeO)) {
							iter.previous();
							continue; // will break areal connectivity so omit?
						}
						if (triChecker.removeable(nodeS, nodeE, nodeO)) {
							modified = true;
							nodeSet.add(nodeO);
							iter.previous(); // move iter before nodeE
							iter.add(nodeO); // insert nodeO between nodeS and nodeE (and before iter)
							iter.previous(); // iter before nodeO
							iter.previous(); // iter before nodeS
						} else {
							iter.previous(); // iter before nodeE
						}
					}
				}
			}
			//
			Coordinate[] newCoords = new Coordinate[hullList.size()];
			hullList.toArray(newCoords);
			return gf.createPolygon(newCoords);
		} else {
			return null;
		}
	}

	/** breadth first digging, supports multi parts
	 * @param triChecker triangle checker to be used
	 * @param allowMultiParts if multiple parts are to be generated
	 * @param keepLineSeg if degenerated line segments should be kept
	 * @return a collection of geometry that form the concave hull of the input data (may contains linestring as degenerated segments)
	 */
	public Collection<Geometry> getConcaveHullBFS(TriangleChecker triChecker, boolean allowMultiParts, boolean keepLineSeg) {
		if (triChecker != null) {
			//TreeSet<Coordinate> nodeSet = new TreeSet<Coordinate>(hullDT);
			LinkedList<DLCirList> hulls = new LinkedList<DLCirList>(); // 
			Vector<DLCirList> rltHulls = new Vector<DLCirList>(); // finished hulls

			Vector<LineString> rltLS = new Vector<LineString>();
			Vector<Point> rltPt = new Vector<Point>();

			TreeSet<HullEdgeCir> edgeIdx = new TreeSet<HullEdgeCir>();

			TreeMap<Coordinate, DLNode<Coordinate>> coordNodeMap = new TreeMap<Coordinate, DLNode<Coordinate>>();
			HashMap<DLNode<Coordinate>, HullEdgeCir> nodeEdgeMap = new HashMap<DLNode<Coordinate>, HullEdgeCir>();

			DLCirList<Coordinate> hull = generateHullEdgeRep(hullDT);

			hulls.add(hull);
			while (!hulls.isEmpty()) {
				hull = hulls.pollFirst();
				generateHullIndices(hull, coordNodeMap, edgeIdx, nodeEdgeMap);
				boolean addHull = true;
				int edgeCnt = edgeIdx.size();
				while (!edgeIdx.isEmpty()) {
					HullEdgeCir edge = edgeIdx.pollLast(); // the longest

					DLNode<Coordinate> sn = edge.sn;
					DLNode<Coordinate> en = sn.getNext();
					Coordinate nodeS = sn.getObj();
					Coordinate nodeE = en.getObj();
					QuadEdge qe = sd.locate(nodeS, nodeE);
					if (qe == null) {
						System.out.println("fail to find edge: " + nodeS.toString() + " -" + nodeE.toString());
						return null;
					}
					QuadEdge lnext = qe.lNext();
					Vertex verO = lnext.dest(); // "opposite" vertex
					Coordinate nodeO = verO.getCoordinate();
					if (triChecker.removeable(nodeS, nodeE, nodeO)) {
						if (hull.size() > 3) {
							if (coordNodeMap.containsKey(nodeO)) { // nodeO is a boundary node, split takes place
								if (allowMultiParts) {
									DLNode<Coordinate> on = coordNodeMap.get(nodeO);
									// check if a tri-corner
									if (on == en.getNext()) {// trim en
										HullEdgeCir ee = nodeEdgeMap.get(en);
										edgeIdx.remove(ee); // se popped out already
										coordNodeMap.remove(nodeE);
										nodeEdgeMap.remove(en);
										nodeEdgeMap.remove(sn);
										hull.remove(en); // sn now connected to on
										//
										HullEdgeCir seNew = new HullEdgeCir(sn);
										nodeEdgeMap.put(sn, seNew);
										edgeIdx.add(seNew);
										if (keepLineSeg) {
											Coordinate[] lsCoords = new Coordinate[2];
											lsCoords[0] = nodeE;
											lsCoords[1] = nodeO;
											rltLS.add(gf.createLineString(lsCoords));
										}
										rltPt.add(gf.createPoint(en.getObj()));
									} else if (on == sn.getPrev()) { // trim sn
										HullEdgeCir oe = nodeEdgeMap.get(on);
										edgeIdx.remove(oe); // se popped out already
										coordNodeMap.remove(nodeS);
										nodeEdgeMap.remove(sn);
										nodeEdgeMap.remove(on);
										hull.remove(sn); //on now connected to en
										//
										HullEdgeCir oeNew = new HullEdgeCir(on);
										nodeEdgeMap.put(on, oeNew);
										edgeIdx.add(oeNew);
										if (keepLineSeg) {
											Coordinate[] lsCoords = new Coordinate[2];
											lsCoords[0] = nodeO;
											lsCoords[1] = nodeS;
											rltLS.add(gf.createLineString(lsCoords));
										}
										rltPt.add(gf.createPoint(sn.getObj()));
									} else {// split
										addHull = false;
										// split
										DLCirList hull2 = hull.split(sn, en, on);
										hulls.add(hull);
										hulls.add(hull2);
										//
										break;
									}
								} else {// multiparts not allowed
									// don't do anything
								}
								//
							} else { // normal digging
								//
								nodeEdgeMap.remove(sn);
								DLNode<Coordinate> on = new DLNode<Coordinate>(nodeO);
								coordNodeMap.put(nodeO, on);
								hull.addAfter(sn, on);
								//
								HullEdgeCir seNew = new HullEdgeCir(sn);
								HullEdgeCir oe = new HullEdgeCir(on);
								edgeIdx.add(seNew);
								edgeIdx.add(oe);
								nodeEdgeMap.put(sn, seNew);
								nodeEdgeMap.put(on, oe);
							}
						} else {// 3 vertices only, clapse to a line segement, may be saved separately if needed?
							addHull = false;
							break;
						}
					}
				}// end while(!edgeIdx.isEmtpy())
				// 
				if (addHull) {
					// add hull to result
					rltHulls.add(hull);
				}
				// clear indices
				coordNodeMap.clear();
				nodeEdgeMap.clear();
				edgeIdx.clear();
			}
			Vector<Geometry> rtn = new Vector<Geometry>(hulls.size() + rltLS.size() + rltPt.size());
			for (DLCirList<Coordinate> h : rltHulls) {
				Coordinate[] coords = new Coordinate[h.size() + 1];
				int cnt = 0;
				DLNode<Coordinate> node = h.getNode();
				do {
					Coordinate coord = node.getObj();
					coords[cnt++] = coord;
					node = node.getNext();
				} while (node != h.getNode());
				coords[cnt] = new Coordinate(coords[0]);
				Geometry geom = gf.createPolygon(coords);
				rtn.add(geom);
			}
			rtn.addAll(rltLS);
			rtn.addAll(rltPt);
			//
			return rtn;
		} else {
			return null;
		}
	}    //

	/** Experimental Concave hull construction with an extra metric to control the order of boundary edge "digging"
	 * @param triChecker
	 * @param m TriangleMetric for controlling the order of digging (default is the length of the boundary edge)
	 * @param allowMultiParts
	 * @param keepLineSeg
	 * @return
	 */
	public Collection<Geometry> getConcaveHullMetric(TriangleChecker triChecker, TriangleMetric m, boolean allowMultiParts, boolean keepLineSeg) {
		if (triChecker != null) {
			LinkedList<DLCirList> hulls = new LinkedList<DLCirList>(); // 
			Vector<DLCirList> rltHulls = new Vector<DLCirList>(); // finished hulls

			Vector<LineString> rltLS = new Vector<LineString>();

			TreeSet<HullEdgeCir> edgeIdx = new TreeSet<HullEdgeCir>();

			TreeMap<Coordinate, DLNode<Coordinate>> coordNodeMap = new TreeMap<Coordinate, DLNode<Coordinate>>();
			HashMap<DLNode<Coordinate>, HullEdgeCir> nodeEdgeMap = new HashMap<DLNode<Coordinate>, HullEdgeCir>();

			DLCirList<Coordinate> hull = generateHullEdgeRep(hullDT);

			hulls.add(hull);
			while (!hulls.isEmpty()) {
				hull = hulls.pollFirst();
				generateHullIndices(hull, m, coordNodeMap, edgeIdx, nodeEdgeMap);
				boolean addHull = true;
				while (!edgeIdx.isEmpty()) {
					HullEdgeCir edge = edgeIdx.pollLast(); // the longest

					DLNode<Coordinate> sn = edge.sn;
					DLNode<Coordinate> en = sn.getNext();
					Coordinate nodeS = sn.getObj();
					Coordinate nodeE = en.getObj();
					QuadEdge qe = sd.locate(nodeS, nodeE);
					if (qe == null) {
						System.out.println("fail to find edge: " + nodeS.toString() + " -" + nodeE.toString());
						return null;
					}
					QuadEdge lnext = qe.lNext();
					Vertex verO = lnext.dest();
					Coordinate nodeO = verO.getCoordinate();
					if (triChecker.removeable(nodeS, nodeE, nodeO)) {
						if (hull.size() > 3) {
							if (coordNodeMap.containsKey(nodeO)) { // nodeO is a boundary node, split takes place
								DLNode<Coordinate> on = coordNodeMap.get(nodeO);
								// check if a tri-corner
								if (on == en.getNext()) {// trim en
									HullEdgeCir ee = nodeEdgeMap.get(en);
									edgeIdx.remove(ee); // se popped out already
									coordNodeMap.remove(nodeE);
									nodeEdgeMap.remove(en);
									nodeEdgeMap.remove(sn);
									hull.remove(en); // sn now connected to on
									//
									HullEdgeCir seNew = new HullEdgeCir(sn, m);
									nodeEdgeMap.put(sn, seNew);
									edgeIdx.add(seNew);
									if (keepLineSeg) {
										Coordinate[] lsCoords = new Coordinate[2];
										lsCoords[0] = nodeE;
										lsCoords[1] = nodeO;
										rltLS.add(gf.createLineString(lsCoords));
									}
								} else if (on == sn.getPrev()) { // trim sn
									HullEdgeCir oe = nodeEdgeMap.get(on);
									edgeIdx.remove(oe); // se popped out already
									coordNodeMap.remove(nodeS);
									nodeEdgeMap.remove(sn);
									nodeEdgeMap.remove(on);
									hull.remove(sn); //on now connected to en
									//
									HullEdgeCir oeNew = new HullEdgeCir(on, m);
									nodeEdgeMap.put(on, oeNew);
									edgeIdx.add(oeNew);
									if (keepLineSeg) {
										Coordinate[] lsCoords = new Coordinate[2];
										lsCoords[0] = nodeO;
										lsCoords[1] = nodeS;
										rltLS.add(gf.createLineString(lsCoords));
									}
								} else {// split
									if (allowMultiParts) {
										addHull = false;
										// split
										DLCirList hull2 = hull.split(sn, en, on);
										hulls.add(hull);
										hulls.add(hull2);
										//
										break;
									}
								}
								//
							} else { // normal digging
								//
								nodeEdgeMap.remove(sn);
								DLNode<Coordinate> on = new DLNode<Coordinate>(nodeO);
								coordNodeMap.put(nodeO, on);
								hull.addAfter(sn, on);
								//
								HullEdgeCir seNew = new HullEdgeCir(sn, m);
								HullEdgeCir oe = new HullEdgeCir(on, m);
								edgeIdx.add(seNew);
								edgeIdx.add(oe);
								nodeEdgeMap.put(sn, seNew);
								nodeEdgeMap.put(on, oe);
							}
						} else {// 3 vertices only, clapse to a line segement, may be saved separately if needed?
							addHull = false;
							break;
						}
					}
				}// end while(!edgeIdx.isEmtpy())
				// 
				if (addHull) {
					// add hull to result
					rltHulls.add(hull);
				}
				// clear indices
				coordNodeMap.clear();
				nodeEdgeMap.clear();
				edgeIdx.clear();
			}
			Vector<Geometry> rtn = new Vector<Geometry>(hulls.size() + rltLS.size());
			for (DLCirList<Coordinate> h : rltHulls) {
				Coordinate[] coords = new Coordinate[h.size() + 1];
				int cnt = 0;
				DLNode<Coordinate> node = h.getNode();
				do {
					Coordinate coord = node.getObj();
					coords[cnt++] = coord;
					node = node.getNext();
				} while (node != h.getNode());
				coords[cnt] = new Coordinate(coords[0]);
				Geometry geom = gf.createPolygon(coords);
				rtn.add(geom);
			}
			rtn.addAll(rltLS);
			//
			return rtn;
		} else {
			return null;
		}
	}

	/** Concave hull with multiple parts and holes (going through all triangles and use union operators to generate results)
	 * @param tcAlpha triangle checker using Alpha shape criterion
	 * @return
	 */
	public Geometry getConcaveHullWithHolesAlpha(TriCheckerAlpha tcAlpha) {
		Coordinate[] hullCoords = new Coordinate[hullDT.size()];
		hullDT.toArray(hullCoords);
		Geometry shell = gf.createPolygon(hullCoords);

		List triCoords = sd.getTriangleCoordinates(false);
		for (Object obj : triCoords) {
			Coordinate[] coords = (Coordinate[]) obj;
			boolean rmvable = tcAlpha.removeable(coords[0], coords[1], coords[2]);
			if (rmvable) {
				Geometry hole = gf.createPolygon(coords);
				shell = shell.difference(hole);
			}
		}
		return shell;
	}
	//

	/** Concave hull with multiple parts and holes (going through all triangles and use union operators to generate results)
	 * @param tcChi triangle checker using Chi criterion (edge length)
	 * @return
	 */
	public Geometry getConcaveHullWithHolesChi(TriCheckerChi tcChi) {
		Coordinate[] hullCoords = new Coordinate[hullDT.size()];
		hullDT.toArray(hullCoords);
		Geometry shell = gf.createPolygon(hullCoords);

		List triCoords = sd.getTriangleCoordinates(false);
		Collection<Geometry> holeCol = new ArrayList<>();
		for (Object obj : triCoords) {
			Coordinate[] coords = (Coordinate[]) obj;
			boolean rmvable0 = tcChi.removeable(coords[0], coords[1], coords[2]);
			boolean rmvable1 = tcChi.removeable(coords[1], coords[2], coords[0]);
			boolean rmvable2 = tcChi.removeable(coords[2], coords[0], coords[1]);
			if (rmvable0 || rmvable1 || rmvable2) {
				Geometry hole = gf.createPolygon(coords);
				holeCol.add(hole);
			}
		}
		try {
			Geometry holes = UnaryUnionOp.union(holeCol);
			if (holes != null && holes.getDimension() > 1) {
				shell = shell.difference(holes);
			}
		} catch (Exception e) {
			LOGGER.error("Error calculating holes", e);
		}
		return shell;
	}

	class HullEdgeCir implements Comparable {
		DLNode<Coordinate> sn;
		double metric;

		public HullEdgeCir(DLNode<Coordinate> sn) {
			this.sn = sn;
			metric = sn.getObj().distance(sn.getNext().getObj());
		}

		public HullEdgeCir(DLNode<Coordinate> sn, TriangleMetric m) {
			this.sn = sn;
			DLNode<Coordinate> en = sn.getNext();
			Coordinate s = sn.getObj();
			Coordinate e = en.getObj();
			QuadEdge qe = sd.locate(s, e);
			if (qe == null) {
				LOGGER.error("fail to find edge: " + s.toString() + " -" + e.toString());
			}
			assert qe != null;
			QuadEdge lnext = qe.lNext();
			Vertex verO = lnext.dest();
			Coordinate o = verO.getCoordinate();

			metric = m.compMetric(s, e, o);
		}

		public int compareTo(Object o) {
			HullEdgeCir other = (HullEdgeCir) o;
			if (metric < other.metric) {
				return -1;
			} else if (metric > other.metric) {
				return 1;
			} else {
				int rlt = sn.getObj().compareTo(other.sn.getObj());
				if (rlt == 0) {
					return (sn.getNext().getObj().compareTo(other.sn.getNext().getObj()));
				} else {
					return rlt;
				}
			}
		}
	}

	class HullEdge implements Comparable {
		Coordinate sp, ep;
		double length;

		public HullEdge(Coordinate start, Coordinate end) {
			sp = start;
			ep = end;
			length = sp.distance(ep);
		}

		public Coordinate getSp() {
			return sp;
		}

		public void setSp(Coordinate sp) {
			this.sp = sp;
		}

		public Coordinate getEp() {
			return ep;
		}

		public void setEp(Coordinate ep) {
			this.ep = ep;
		}

		public double getLength() {
			return length;
		}

		public void setLength(double length) {
			this.length = length;
		}

		public int compareTo(Object o) {
			HullEdge other = (HullEdge) o;
			if (length < other.length) {
				return -1;
			} else if (length > other.length) {
				return 1;
			} else {
				int rlt = sp.compareTo(other.sp);
				if (rlt == 0) {
					return ep.compareTo(other.ep);
				} else {
					return rlt;
				}
			}
		}
	}

	class DLNode<T> {
		DLNode<T> prev, next;
		T obj;

		public DLNode() {
			prev = next = null;
			obj = null;
		}

		public DLNode(T o) {
			prev = next = null;
			obj = o;
		}

		public DLNode(DLNode<T> p, DLNode<T> n) {
			prev = p;
			next = n;
			obj = null;
		}

		public DLNode(DLNode<T> p, DLNode<T> n, T o) {
			prev = p;
			next = n;
			obj = o;
		}

		public DLNode<T> getPrev() {
			return prev;
		}

		public void setPrev(DLNode<T> prev) {
			this.prev = prev;
		}

		public DLNode<T> getNext() {
			return next;
		}

		public void setNext(DLNode<T> next) {
			this.next = next;
		}

		public T getObj() {
			return obj;
		}

		public void setObj(T obj) {
			this.obj = obj;
		}

		/**
		 * @param node
		 */
		public void insertAfter(DLNode node) {
			if (node != null) {
				if (this.next != null) {
					this.next.setPrev(node);
				}
				node.prev = this;
				node.next = this.next;
				this.next = node;
			}
		}

		public DLNode<T> insertAfter(T o) {
			DLNode<T> node = new DLNode(o);
			insertAfter(node);
			return node;
		}

		/**
		 * @param node
		 */
		public void insertBefore(DLNode node) {
			if (node != null) {
				if (this.prev != null) {
					this.prev.next = node;
				}
				node.prev = this.prev;
				node.next = this;
				this.prev = node;
			}
		}

		public DLNode<T> insertBefore(T o) {
			DLNode<T> node = new DLNode(o);
			insertBefore(node);
			return node;
		}

		public DLNode remove() {
			if (this.prev != null) {
				this.prev.next = this.next;
			}
			if (this.next != null) {
				this.next.prev = this.prev;
			}
			DLNode rtn = null;
			if (this.prev != null) {
				rtn = this.prev;
			} else if (this.next != null) {
				rtn = this.next;
			}
			this.prev = this.next = null;
			return rtn;
		}

	}

	class DLCirList<T> {
		DLNode<T> anchor = null;
		int size = 0;

		public DLCirList() {

		}

		public DLCirList(DLNode<T> node) {
			anchor = node;
			if (anchor.next == null && anchor.prev == null) {
				anchor.next = anchor.prev = anchor;
			}
			calculateSize();
		}

		public DLCirList(T o) {
			init(o);
		}

		public DLNode<T> getNode() {
			return anchor;
		}
		
		private void init(T o) {
			anchor = new DLNode<T>(o);
			anchor.next = anchor.prev = anchor;
			size++;
		}

		public void add(T o) {
			if (anchor == null) {
				init(o);
			} else {

				DLNode<T> newNode = new DLNode<T>(o);
				anchor.insertAfter(newNode);
				anchor = newNode;
				size++;
			}
		}

		public void addAfter(DLNode<T> node, DLNode<T> newNode) {
			node.insertAfter(newNode);
			size++;
		}

		public void remove(DLNode<T> node) {
			DLNode<T> rtn = node.remove();
			if (node == anchor) {
				anchor = rtn;
			}
			size--;
		}

		public int calculateSize() {
			if (anchor == null) {
				return size = 0;
			}
			DLNode<T> node = anchor;
			int cnt = 1;
			while (node.next != null && node.next != anchor) {
				node = node.next;
				cnt++;
			}
			return size = cnt;
		}

		public int size() {
			return size;
		}

		/** split the current circular list into two by link n1 and n2; current list contains edge n1-n2 and new list containing edge n2-n1 will be returned
		 * @param n1
		 * @param n2
		 * @return
		 */
		public DLCirList<T> split(DLNode<T> n1, DLNode<T> n2) {
			if (n1.next == n2 || n1.prev == n2) {// consecutive nodes
				return null;
			}
			return null;
		}

		/** split, remove ns-ne and connect ns-no and no-ne, new circular list containing no-ne is returned
		 * @param ns
		 * @param ne
		 * @param no
		 * @return
		 */
		public DLCirList<T> split(DLNode<T> ns, DLNode<T> ne, DLNode<T> no) {
			if (ns.next != ne) {
				return null;
			}
			if (size < 3) {
				calculateSize();
				if (size < 3) {
					return null;
				}
			}
			ns.next = no;
			DLNode<T> no2 = new DLNode<T>(no.obj);
			no2.prev = no.prev;
			no.prev.next = no2;
			no2.next = ne;
			ne.prev = no2;
			no.prev = ns;
			anchor = ns;
			calculateSize();
			return new DLCirList(ne);
		}
	}
}
