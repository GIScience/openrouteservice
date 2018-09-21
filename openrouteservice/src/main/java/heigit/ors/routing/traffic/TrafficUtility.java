/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package heigit.ors.routing.traffic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import heigit.ors.mapmatching.RouteSegmentInfo;
import heigit.ors.routing.RoutingProfile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jts.operation.linemerge.LineSequencer;

import java.util.Base64;


@SuppressWarnings("restriction")
public class TrafficUtility {
	
	private static DistanceCalc distCalc = new DistanceCalcEarth();
	// 2015-11-13T14:30:00
	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	public static List<TmcSegment> detectSegments(File segments, File roads, File points, File poffsets,
			RoutingProfile routeProfile) {
		List<TmcSegment> result = new ArrayList<TmcSegment>();
		String line = null;
		int readLines = 0;
		int notSavedSegments = 0;

		try {
			System.out.println("Start ...");

			System.out.println(" Parse TMC Points (Offsets)");
			HashMap<Integer, TmcPoint> getPoffsets = new HashMap<Integer, TmcPoint>();
			BufferedReader brPoffsets = new BufferedReader(new FileReader(poffsets));
			readLines = 0;

			while ((line = brPoffsets.readLine()) != null) {
				if ((!line.startsWith("#")) && (!line.equals(""))) {

					String[] tmp = line.split(";");
					int lcd = -1;
					int neg_lcd = -1;
					int pos_lcd = -1;
					if (!tmp[2].equals(""))
						lcd = Integer.parseInt(tmp[2]);
					if (!tmp[3].equals(""))
						neg_lcd = Integer.parseInt(tmp[3]);
					if ((tmp.length > 4) && (!tmp[4].equals("")))
						pos_lcd = Integer.parseInt(tmp[4]);
					getPoffsets.put(Integer.valueOf(lcd), new TmcPoint(lcd, neg_lcd, pos_lcd));
				}
				readLines++;
			}
			brPoffsets.close();

			System.out.println("  ReadLines: " + readLines + " Saved Points: " + getPoffsets.size());

			int internalID = 1;

			System.out.println(" Parse TMC Points");
			HashMap<Integer, ArrayList<Integer>> getSegmentPoints = new HashMap<Integer, ArrayList<Integer>>();
			BufferedReader brPoints = new BufferedReader(new FileReader(points));
			readLines = 0;
			double y;
			double x;

			while ((line = brPoints.readLine()) != null) {
				if ((!line.startsWith("#")) && (!line.equals(""))) {
					String[] tmp = line.split(";");

					int lcd = -1;
					int seg_lcd = -1;
					if (!tmp[2].equals(""))
						lcd = Integer.parseInt(tmp[2]);
					if (!tmp[12].equals(""))
						seg_lcd = Integer.parseInt(tmp[12]);
					if ((tmp[12].equals("")) && (!tmp[13].equals(""))) {
						seg_lcd = Integer.parseInt(tmp[13]);
					}
					if ((lcd > 0) && (seg_lcd > 0)) {
						x = Double.parseDouble(tmp[22].replace("+", "")) / 100000.0D;
						y = Double.parseDouble(tmp[23].replace("+", "")) / 100000.0D;

						if (getPoffsets.containsKey(Integer.valueOf(lcd))) {
							TmcPoint p = (TmcPoint) getPoffsets.get(Integer.valueOf(lcd));
							getPoffsets.remove(Integer.valueOf(lcd));
							p.setCoordinate(new com.vividsolutions.jts.geom.Coordinate(x, y));
							getPoffsets.put(Integer.valueOf(lcd), p);

							ArrayList<Integer> pointsOfSegment = new ArrayList<Integer>();
							if (getSegmentPoints.containsKey(Integer.valueOf(seg_lcd))) {
								pointsOfSegment = getSegmentPoints.get(Integer.valueOf(seg_lcd));
								getSegmentPoints.remove(Integer.valueOf(seg_lcd));
							}
							pointsOfSegment.add(Integer.valueOf(lcd));
							getSegmentPoints.put(Integer.valueOf(seg_lcd), pointsOfSegment);
						} else {
							System.out.println("No Poffsets for > " + line);
						}
					}
				}

				readLines++;
			}
			brPoints.close();

			System.out.println("  ReadLines: " + readLines + " Saved Segments: " + getSegmentPoints.size());

			System.out.println(" Parse TMC Segments & Roads and than start processing ...");

			File[] files = { segments, roads };
			int d1 = files.length;// (y = files).length;
			for (x = 0; x < d1; x++) {
				File f = files[(int) x];
				BufferedReader reader = new BufferedReader(new FileReader(f));
				readLines = 0;
				while ((line = reader.readLine()) != null) {
					if ((!line.startsWith("#")) && (!line.equals(""))) {
						String[] tmp = line.split(";");

						if (!isInteger(tmp[2]))
							continue;

						int seg_lcd = Integer.parseInt(tmp[2]);
						String roadnumber = tmp[6];

						ArrayList<Integer> pointsOfSegment = (ArrayList<Integer>) getSegmentPoints.get(Integer
								.valueOf(seg_lcd));

						if (pointsOfSegment != null) {
							HashSet<Integer> listOfSegments = new HashSet<Integer>(pointsOfSegment);
							TmcPoint start = null;

							for (Iterator<Integer> localIterator = pointsOfSegment.iterator(); localIterator.hasNext();) {
								int lcd = ((Integer) localIterator.next()).intValue();
								TmcPoint p = (TmcPoint) getPoffsets.get(Integer.valueOf(lcd));
								if (!listOfSegments.contains(Integer.valueOf(p.getNeg_off_lcd()))) {
									if (p.getNeg_off_lcd() == -1)
										start = p;
									else {
										start = (TmcPoint) getPoffsets.get(Integer.valueOf(p.getNeg_off_lcd()));
									}
								}
							}

							if ((start != null) && (listOfSegments.size() > 1)) {
								getSegmentPoints.remove(Integer.valueOf(seg_lcd));
								boolean done = false;
								TmcPoint tmcpPrior = start;
								while (!done) {
									TmcPoint tmcpNext = (TmcPoint) getPoffsets.get(Integer.valueOf(tmcpPrior
											.getPos_off_lcd()));
									if (!listOfSegments.contains(Integer.valueOf(tmcpNext.getPos_off_lcd()))) {
										done = true;
									}

									TmcSegment[] segs = detectSegments(routeProfile, seg_lcd, roadnumber,
											tmcpPrior.getLcd(), tmcpNext.getLcd(), +1, tmcpPrior.getCoordinate(),
											tmcpNext.getCoordinate());

									if (segs != null) {
										if (segs[0] != null) {
											result.add(segs[0]);
											internalID++;
										} else {
											System.out.println("  > Problem @ Segment: " + seg_lcd + " Locations: "
													+ tmcpPrior.getLcd() + "+" + tmcpNext.getLcd());
											notSavedSegments++;
										}

										if (segs[1] != null) {
											result.add(segs[1]);
											internalID++;
										} else {
											System.out.println("  > Problem @ Segment: " + seg_lcd + " Locations: "
													+ tmcpNext.getLcd() + "-" + tmcpPrior.getLcd());
											notSavedSegments++;
										}
									}

									tmcpPrior = tmcpNext;
								}
							}
						}

						if (readLines % 100 == 0) {
							System.out.println("  ... " + readLines + " Segments done, calculated Routes: "
									+ internalID);
						}
					}
					readLines++;
				}
				reader.close();
			}

			System.out.println("  ReadLines: " + readLines);
			System.out.println("  Number of Segments which are *not* saved (LCL Problems): " + getSegmentPoints.size());
			System.out.println("  Number of Segments which are *not* saved (No Routing): " + notSavedSegments);

			System.out.println("... End!");
		} catch (Exception ex) {
			System.out.println("Line: " + readLines + " >> " + line);
			ex.printStackTrace();
		}

		return result;
	}

	public static TmcSegment[] detectSegments(RoutingProfile rp, long id, String roadnumber, Integer startID, Integer endID,
			Integer direction, Coordinate startCoordinate, Coordinate endCoordinate) {
		TmcSegment[] result = new TmcSegment[2];

		try {
			/*if (DebugUtility.isDebug())
			{
				int from = 46277;//10139;
				int to = 46278;//10140;

				if (!((startID == from && endID == to) || (startID == to && endID == from)))
					return null;
			}*/
			Coordinate[] locations = new Coordinate[2];
            locations[0] = startCoordinate;
            locations[1] = endCoordinate;
            
			double segmentDist = distCalc.calcDist(startCoordinate.y, startCoordinate.x, endCoordinate.y,
					endCoordinate.x);
			
			RouteSegmentInfo[] rsiArray = null;
			
			for (int t = 0; t < 3; t++) {
				double searchRadius = 50;
				double threshold = 0.3;
				
				if (t == 1)
				{
					searchRadius = 200;
					threshold = 0.4;
				}
				else if (t == 2)
				{
					searchRadius = 700;
					threshold = 0.45;
				}

				rsiArray = rp.getMatchedSegments(locations, searchRadius, true);

				if (rsiArray != null) {
					if (Math.abs(rsiArray[0].getDistance() - segmentDist) / segmentDist > threshold)
						rsiArray = null;
				}
				
				if (rsiArray != null) // && rsiArray.length == 2 && rsiArray[1] != null)
					break;
			}
/*
			if (false) {
				// below is a special case when one of the points is to far from any meaningful road. see pair 10139 - 10140

				double dx = endCoordinate.x - startCoordinate.x;
				double dy = endCoordinate.y - startCoordinate.y;
				if (Math.abs(dx) > 0 || Math.abs(dy) > 0) {
					double s = 1.0 / Math.sqrt(dx * dx + dy * dy);
					double vecX = -dy * s;
					double vecY = dx * s;
					
					RouteSegmentInfo[] bestRsi = null;
					double minRouteDist = Double.MAX_VALUE;
					int n = 5;

					for (int i = -n; i <= n; i++) {
						if (i == 0)
							continue;

						double d = Math.signum(i)
								* metersToDecimalDegrees(Math.abs(i * searchRadius), startCoordinate.y);
						double x0 = startCoordinate.x + vecX * d;
						double y0 = startCoordinate.y + vecY * d;

						rsiArray = rp.getMatchedSegments(y0, x0, endCoordinate.y, endCoordinate.x, searchRadius, true);
						if (rsiArray != null) {
							RouteSegmentInfo rsi = rsiArray[0];
							if (rsi != null && !rsi.isEmpty()) {
								if (minRouteDist > rsi.getDistance()) {
									minRouteDist = rsi.getDistance();
									bestRsi = rsiArray;
									
									if (minRouteDist < 1.1*segmentDist)
										break;
								}
							}
						}
					}

					for (int i = -n; i < n; i++) {
						if (i == 0)
							continue;

						double d = Math.signum(i)
								* metersToDecimalDegrees(Math.abs(i * searchRadius), endCoordinate.y);
						double x0 = endCoordinate.x + vecX * d;
						double y0 = endCoordinate.y + vecY * d;

						rsiArray = rp.getMatchedSegments(startCoordinate.y, startCoordinate.x, y0, x0, searchRadius,
								true);
						if (rsiArray != null) {
							RouteSegmentInfo rsi = rsiArray[0];
							if (rsi != null && !rsi.isEmpty()) {
								if (minRouteDist > rsi.getDistance()) {
									minRouteDist = rsi.getDistance();
									bestRsi = rsiArray;
									
									if (minRouteDist < 1.1*segmentDist)
										break;
								}
							}
						}
					}

					if (bestRsi != null &&  bestRsi[0].getDistance() > 1.4*segmentDist)
						rsiArray = null;
					else
						rsiArray = bestRsi;
				}
			}
*/
			if (rsiArray != null)
			{
				RouteSegmentInfo rsi = rsiArray[0];
				 if (rsi != null)
				 {
					 if  (!rsi.isEmpty())
					 {
						 result[0] = new TmcSegment(id, roadnumber, startID, endID, 0, rsi.getDistance(),
								 rsi.getGeometry(), rsi.getEdges());
					 }
				 }
				 
				 rsi = rsiArray[1];
				 if (rsi != null)
				 {
					 if  (!rsi.isEmpty())
					 {
						 result[1] = new TmcSegment(id, roadnumber, endID, startID, 1, rsi.getDistance(),
								 rsi.getGeometry(), rsi.getEdges());
					 }
				 }
			}
		} catch (Exception ex) {
			return null;
		}

		return result;
	}

	public static boolean isInteger(String in) {
		if (in != null) {
			char c;
			int i = 0;
			int l = in.length();
			if (l > 0 && in.charAt(0) == '-') {
				i = 1;
			}
			if (l > i) {
				for (; i < l; i++) {
					c = in.charAt(i);
					if (c < '0' || c > '9')
						return false;
				}
				return true;
			}
		}
		return false;
	}
	
	public static String getData(String address) {

		StringBuffer buffer = new StringBuffer();
		try {
			URL url = new URL(address);
			URLConnection con = url.openConnection();
			byte[] encodedPassword = Base64.getEncoder().encode("doof:letmein2011".getBytes());
			con.setRequestProperty("Authorization", "Basic " + encodedPassword);
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {

				buffer.append(inputLine + "\n");
			}
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return buffer.toString();
	}

	public static List<TrafficFeatureInfo> extractTmcFeatures(String tmcMessage, TmcSegmentsCollection segments,
			long timeThreshold, TrafficLocationGraph graph, Logger logger) {
		List<TrafficFeatureInfo> result = new ArrayList<TrafficFeatureInfo>();

		List<TrafficMessageData> messages = TrafficMessageData.parse(tmcMessage, "ISO-8859-1", segments, graph);

		GeometryFactory geomFactory = new GeometryFactory();

		for (int i = 0; i < messages.size(); i++) {
			try {
				TrafficMessageData m = (TrafficMessageData) messages.get(i);

				// event codes see
				// http://wiki.openstreetmap.org/wiki/TMC/Event_Code_List

				if (m.locationCodes.size() > 1) {
					boolean bAdd = false;
					List<Geometry> geoms = new ArrayList<Geometry>();
					List<Geometry> geoms_reverse = new ArrayList<Geometry>();
					List<Integer> edgeIds = new ArrayList<Integer>();
					List<Integer> edgeIds_reverse = new ArrayList<Integer>();

					for (int j = 0; j < m.locationCodes.size() - 1; j++) {
						Integer lcl_fr = m.locationCodes.get(j);
						Integer lcl_to = m.locationCodes.get(j + 1);
						
						for (int si = 0; si < segments.size(); si++) {
							TmcSegment seg = segments.get(si);

							int osm_fr = seg.getFrom();
							int osm_to = seg.getTo();

							if ((lcl_fr == osm_fr && lcl_to == osm_to) || (lcl_fr == osm_to && lcl_to == osm_fr)) {
								if (m.bothDirections || m.direction != seg.getDirection()) {
									/*
									 * in unserem XML kodiert das Element LDR
									 * die Richtungsangabe. Dabei ist 0 =
									 * positive Richtung und 1 = negative
									 * Richtung. Dies bezieht sich auf die
									 * Verknüpfungsrichtung in der TMC Tabelle.
									 * Beispiel: In der TMC Tabelle ist A
									 * positiv verknüpft ist mit B. Auf der
									 * Strecke von A nach B ist bei B ein Unfall
									 * passiert und es Staut sich zurück bis
									 * nach A.
									 * 
									 * Dann ist die Meldung wie folgt kodiert:
									 * PrimaryLocation = B (hier ist der Unfall)
									 * SecondaryLocation = A (bis hier reicht
									 * der Stau) LocationDirection = 1 (negativ)
									 * 
									 * Die LocationDirection (LDR) ist negative,
									 * weil man vom PrimaryLocation zum
									 * SecondaryLocation nur kommt, indem man
									 * sich rückwärts (negativ) durch die TMC
									 * Verknüpfung hangelt.
									 */
									if ((lcl_fr == osm_to && lcl_to == osm_fr))
									{
										geoms_reverse.add(seg.getGeometry());
										edgeIds_reverse.addAll(seg.getEdgeIDs());
									}
									else
									{
										geoms.add(seg.getGeometry());
										edgeIds.addAll(seg.getEdgeIDs());
									}
									bAdd = true;
								}
							}
						}
					}
					
					if (bAdd)
					{
						List<TrafficFeatureInfo> tei = createTrafficFeatureInfo(m, edgeIds, geoms, geomFactory, timeThreshold);
						if (tei != null)
							result.addAll(tei);
						
						List<TrafficFeatureInfo> tei_reverse = createTrafficFeatureInfo(m, edgeIds_reverse, geoms_reverse, geomFactory, timeThreshold);
						if (tei_reverse != null)
							result.addAll(tei_reverse);
					}
				} else {
					if (m.location != null)
					{
						TrafficFeatureInfo tei = new TrafficFeatureInfo(m.eventDataCodes, m.messageDataText, null);
						tei.setGeometry(geomFactory.createPoint(m.location));
						result.add(tei);
					}
				}
			} catch (Exception e) {
				logger.warning(e.getMessage());
			}
		}

		return result;
	}
	
	private static List<TrafficFeatureInfo> createTrafficFeatureInfo(TrafficMessageData m, List<Integer> edgeIds, List<Geometry> geoms, GeometryFactory geomFactory, long timeThreshold) throws ParseException
	{
		if (geoms.size() == 0)
			return null;
		
		List<TrafficFeatureInfo> result = new ArrayList<TrafficFeatureInfo>();
		Date now = new Date();
		
		for(Geometry geom : processGeometries(geoms, geomFactory))
		{
			TrafficFeatureInfo tei = new TrafficFeatureInfo(m.eventDataCodes,
					m.messageDataText, edgeIds);
			tei.setGeometry(geom);
			
			if (!Helper.isEmpty(m.expTime) && Helper.isEmpty(m.endTime)) {
				Date expTime = df.parse(m.expTime);
				if (now.compareTo(expTime) > 0)
					return null;
			} else {
				if (!Helper.isEmpty(m.startTime) || !Helper.isEmpty(m.endTime)) {
					Date beginTime = Helper.isEmpty(m.startTime) ? null : df.parse(m.startTime);
					Date endTime = Helper.isEmpty(m.endTime) ? null : df.parse(m.endTime);
					tei.setDuration(beginTime, endTime);

					if (timeThreshold > 0 && (!Helper.isEmpty(m.uptTime) && Helper.isEmpty(m.endTime))) {
						Date uptTime = Helper.isEmpty(m.uptTime) ? null : df.parse(m.uptTime);
						if (endTime != null) {
							if (uptTime.compareTo(endTime) > 0)
								return null;
						}
					}
				} else {
					/*if (timeThreshold > 0 && !Helper.isEmpty(m.uptTime)) {
					Date uptTime = Helper.isEmpty(m.uptTime) ? null : df.parse(m.uptTime);

					long diff = now.getTime() - uptTime.getTime();
					if (diff > timeThreshold)
						continue;
				}*/
				}
			}
			
			result.add(tei);
		}
		
		return result.size() == 0 ? null: result;
	}
	
	private static List<Geometry> processGeometries(List<Geometry> geoms, GeometryFactory geomFactory)
	{
		List<Geometry> result = new ArrayList<Geometry>();
		
		if (geoms.size() > 1) 
		{
			// Create one LineString from a set of LineStrings.
			LineMerger lineMerger = new LineMerger();
			lineMerger.add(geoms);
			
			LineSequencer lineSeq = new LineSequencer();
			lineSeq.add(lineMerger.getMergedLineStrings());
			
			if (lineSeq.isSequenceable())
			{
				Geometry geomSeq = lineSeq.getSequencedLineStrings();
				
				if (geomSeq instanceof LineString)
				{
					result.add(geomSeq);
				}
				else
				{
					MultiLineString mls = (MultiLineString)geomSeq;
					
					List<Geometry> geoms2 = new ArrayList<Geometry>();
					for(int gi = 0; gi < mls.getNumGeometries(); gi++)
						geoms2.add(mls.getGeometryN(gi));
					
					if (canMergeGeometries(geoms2))
						result.add(mergeGeometries(geoms2, geomFactory));
					else
						result.addAll(geoms2);
				}
			}
			else
			{
				if (canMergeGeometries(geoms))
					result.add(mergeGeometries(geoms, geomFactory));
				else
					result.addAll(geoms);
			}
		}
		else
		{
			result.add(geoms.get(0));
		}
		
		return result;
	}
	
	private static boolean canMergeGeometries(List<Geometry> geoms)
	{
		int i = 0;
		Coordinate pLast = null;
		
		for(Geometry geom : geoms)
		{
			LineString ls = (LineString)geom;
			
			if (i > 0)
			{
				int nCoords = ls.getNumPoints();
				Coordinate pEnd = pLast;
				Coordinate p0 = ls.getCoordinateN(0);
				Coordinate pN = ls.getCoordinateN(nCoords - 1);
				double dist0 = distCalc.calcDist(pEnd.y,  pEnd.x, p0.y, p0.x);
				double distN = distCalc.calcDist(pEnd.y,  pEnd.x, pN.y, pN.x);
				if (dist0 > 15 && distN > 15)
					return false;
			}
			
			pLast = ls.getCoordinateN(ls.getNumPoints() - 1);
			
			i++;
		}
		
		return true;
	}
	
	private static Geometry mergeGeometries(List<Geometry> geoms, GeometryFactory geomFactory)
	{
		List<Coordinate> coords = new ArrayList<Coordinate>();

		int i = 0;
		
		for(Geometry geom : geoms)
		{
			LineString ls = (LineString)geom;
			
			int nCoords = ls.getNumPoints();
			
			if (i > 0)
			{
				Coordinate pEnd = coords.get(coords.size() - 1);
				Coordinate p0 = ls.getCoordinateN(0);
				Coordinate pN = ls.getCoordinateN(nCoords - 1);
				double dist0 = distCalc.calcDist(pEnd.y,  pEnd.x, p0.y, p0.x);
				double distN = distCalc.calcDist(pEnd.y,  pEnd.x, pN.y, pN.x);
				if (distN < dist0)
					ls.reverse();
			}
			
			for(int ci = 0; ci < nCoords; ci++)
			{
				Coordinate c = ls.getCoordinateN(ci);
				coords.add(c);
			}
			
			i++;
		}

		LineString ls = geomFactory.createLineString(coords.toArray(new Coordinate[coords.size()]));
		
		return ls;
	}

	public static Date getMessageDateTime(String tmcMessage) throws ParserConfigurationException, SAXException,
			IOException, ParseException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		InputStream stream = new ByteArrayInputStream(tmcMessage.getBytes(StandardCharsets.UTF_8));
		Document doc = docBuilder.parse(stream);

		doc.getDocumentElement().normalize();
		String timeStamp = doc.getDocumentElement().getAttribute("FGT");

		// FGT="2014-05-01T15:06:00"
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		return formatter.parse(timeStamp);
	}

/*
	public static void saveMatchedTmcDataToFile(List<TrafficFeatureInfo> tmcFeatures, String fileName) {
		if (tmcFeatures != null) {
			FeatureSchema fs = new FeatureSchema();

			fs.addAttribute("id", AttributeType.INTEGER);
			fs.addAttribute("codes", AttributeType.STRING);
			fs.addAttribute("message", AttributeType.STRING);
			fs.addAttribute("start_time", AttributeType.STRING);
			fs.addAttribute("end_time", AttributeType.STRING);
			fs.addAttribute("geometry", AttributeType.GEOMETRY);
			FeatureCollection fc = new FeatureDataset(fs);

			int i = 0;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

			for (TrafficFeatureInfo tfi : tmcFeatures) {
				String message = tfi.getMessage();
				if (message != null && message.length() > 255) {
					message = message.substring(0, 255);
				}

				Feature feat = new BasicFeature(fs);
				feat.setAttribute("id", Integer.valueOf(i));
				feat.setAttribute("codes", tfi.getEventCodesAsString());
				feat.setAttribute("message", message);
				feat.setAttribute("start_time",
						tfi.getStartTime() == null ? null : dateFormat.format(tfi.getStartTime()));
				feat.setAttribute("end_time", tfi.getEndTime() == null ? null : dateFormat.format(tfi.getEndTime()));
				feat.setGeometry(tfi.getGeometry());
				fc.add(feat);

				i++;
			}

			File output = new File(fileName);

			switch (FileUtility.getExtension(fileName).toLowerCase()) {
			case ".shp":
				ShapefileWriter shapeWriter = new ShapefileWriter();
				DriverProperties prop = new DriverProperties();
				prop.set("File", output.getAbsolutePath());
				prop.set("ShapeType", "xy");
				try {
					shapeWriter.write(fc, prop);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				break;
			case ".json":

				break;
			}
		}
	}

	public static void saveTmcSegmentsToShapefile(List<TmcSegment> segments, String shapeFileName) {
		if (segments != null) {
			FeatureSchema fs = new FeatureSchema();

			fs.addAttribute("id", AttributeType.INTEGER);
			fs.addAttribute("segment", AttributeType.INTEGER);
			fs.addAttribute("direction", AttributeType.INTEGER);
			fs.addAttribute("from", AttributeType.INTEGER);
			fs.addAttribute("to", AttributeType.INTEGER);
			fs.addAttribute("tmc", AttributeType.STRING);
			fs.addAttribute("roadnumber", AttributeType.STRING);
			fs.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
			FeatureCollection fc = new FeatureDataset(fs);

			for (int si = 0; si < segments.size(); si++) {
				TmcSegment seg = segments.get(si);
				if (seg.getGeometry() != null && !seg.getGeometry().isEmpty()) {
					Feature feat = new BasicFeature(fs);
					feat.setAttribute("id", Integer.valueOf(si));
					feat.setAttribute("segment", Integer.valueOf((int) seg.getId()));
					feat.setAttribute("direction", Integer.valueOf((int) seg.getDirection()));
					feat.setAttribute("from", Integer.valueOf((int) seg.getFrom()));
					feat.setAttribute("to", Integer.valueOf((int) seg.getTo()));
					String direction = seg.getDirection() < 0 ? "-" : "+";
					feat.setAttribute("tmc", "DE:" + seg.getFrom() + direction + seg.getTo());
					feat.setAttribute("roadnumber", seg.getRoadnumber());
					feat.setGeometry(seg.getGeometry());
					fc.add(feat);
				}
			}

			File output = new File(shapeFileName);

			ShapefileWriter shapeWriter = new ShapefileWriter();
			DriverProperties prop = new DriverProperties();
			prop.set("File", output.getAbsolutePath());
			prop.set("ShapeType", "xy");
			try {
				shapeWriter.write(fc, prop);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}
*/
}
