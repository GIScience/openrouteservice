/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

// Authors:  E. Steiger 

package org.freeopenls.routeservice.traffic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

public class TrafficMessageData {
	int direction = 0;
	boolean bothDirections = false;
	List<String> messageDataText = new ArrayList<String>();
	List<Long> locationCodes = new ArrayList<Long>();
	List<Integer> eventDataCodes = new ArrayList<Integer>();
	Coordinate location;
	String startTime;
	String endTime;
	String uptTime;
	String expTime;

	public TrafficMessageData() {
	}

	@SuppressWarnings("unchecked")
	public static List<TrafficMessageData> parse(String message, String cs, TmcSegmentsCollection tmcSegments, TrafficLocationGraph graph) {
		ArrayList<TrafficMessageData> messages = new ArrayList<TrafficMessageData>();

		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			InputStream stream = new ByteArrayInputStream(message.getBytes(cs));
			Document doc = docBuilder.parse(stream);

			String timeStamp = "";
			ArrayList<String> messageDataText = new ArrayList<String>();
			ArrayList<Long> locationCodes = new ArrayList<Long>();
			ArrayList<Integer> eventDataCodes = new ArrayList<Integer>();
			ArrayList<Coordinate> locations = new ArrayList<Coordinate>();

			String startTime = null;
			String endTime = null;
			String uptTime = null;
			String expTime = null;

			doc.getDocumentElement().normalize();
			timeStamp = doc.getDocumentElement().getAttribute("FGT");
			System.out.println("FGT: " + timeStamp);

			NodeList listOfINFs = doc.getElementsByTagName("IFN");
			for (int i = 0; i < listOfINFs.getLength(); i++) {
				Node ifn = listOfINFs.item(i);
				Element ifnElement = (Element) ifn;

				NodeList listOfMNGs = ifnElement.getElementsByTagName("MNG");
				for (int j = 0; j < listOfMNGs.getLength(); j++) {
					Node mng = listOfMNGs.item(j);
					NodeList mngChildNodes = mng.getChildNodes();
					for (int a = 0; a < mngChildNodes.getLength(); a++) {
						Node mngChild = mngChildNodes.item(a);
						if (mngChild.getNodeName().equals("UPT")) {
							// <UPT>2015-08-17T06:00:01</UPT>
							uptTime = mngChild.getTextContent();
						} else if (mngChild.getNodeName().equals("EXP")) { // this
							// element
							// is
							// not
							// provided
							// in
							// TIC3
							// <EXP>2015-08-17T06:00:01</EXP>
							expTime = mngChild.getTextContent();
						}

					}
				}

				NodeList listOfMESs = ifnElement.getElementsByTagName("MES");
				for (int j = 0; j < listOfMESs.getLength(); j++) {
					Node mes = listOfMESs.item(j);
					Element mesElement = (Element) mes;
					if (mes.getParentNode().getNodeName().equals("TRA")) {
						NodeList listOfMDAs = mesElement.getElementsByTagName("MDA");
						for (int k = 0; k < listOfMDAs.getLength(); k++) {
							Node mda = listOfMDAs.item(k);

							/*
							 * <MDA> <MDC>3</MDC> <MDT>Zwischen Auffahrt B43,
							 * ...</MDT> </MDA>
							 */
							NodeList mdaChildNodes = mda.getChildNodes();
							for (int a = 0; a < mdaChildNodes.getLength(); a++) {
								Node mdaChild = mdaChildNodes.item(a);

								if (mdaChild.getNodeName().equals("MDT")) {
									String msg = mdaChild.getTextContent();
									if (!Helper.isEmpty(msg)) {
										messageDataText.add(msg);
										break;
									}
								}
								/*
								 * if (mdaChild.getNodeName().equals("MDC")) {
								 * String index = mdaChild.getTextContent();
								 * //if ("2".equals(index)) // Start from MDC2,
								 * as MDC0 and MDC1 describe road name and
								 * region { mdaChild = mdaChildNodes.item(a+1);
								 * 
								 * if (mdaChild.getNodeName().equals("MDT")) {
								 * String msg = mdaChild.getTextContent(); if
								 * (!Helper.isEmpty(msg)) {
								 * messageDataText.add(msg); break; } } } }
								 */
							}
						}
					}
				}

				// <TTI><TSA>2015-10-23T13:30:00</TSA><TSO>2015-11-13T14:30:00</TSO></TTI>
				NodeList listOfTTIs = ifnElement.getElementsByTagName("TTI");
				for (int j = 0; j < listOfTTIs.getLength(); j++) {
					Node tti = listOfTTIs.item(j);

					NodeList tsChildNodes = tti.getChildNodes();
					for (int k = 0; k < tsChildNodes.getLength(); k++) {
						Node tsChild = tsChildNodes.item(k);
						if (tsChild.getNodeName().equals("TSA")) {
							startTime = tsChild.getTextContent();
						}
						if (tsChild.getNodeName().equals("TSO")) {
							endTime = tsChild.getTextContent();
						}
					}
				}

				NodeList listOfLCDs = ifnElement.getElementsByTagName("LCD");
				for (int j = 0; j < listOfLCDs.getLength(); j++) {
					locationCodes.add(Long.parseLong(listOfLCDs.item(j).getTextContent()));
				}
				NodeList listOfECOs = ifnElement.getElementsByTagName("ECO");
				for (int j = 0; j < listOfECOs.getLength(); j++) {
					eventDataCodes.add(Integer.parseInt(listOfECOs.item(j).getTextContent()));
				}
				NodeList listOfLDRs = ifnElement.getElementsByTagName("LDR");

				int direction = Integer.parseInt(listOfLDRs.item(0).getTextContent());
				boolean bothDirections = ifnElement.getElementsByTagName("EBF").item(0).getTextContent().equals("1");

				// We try to cope with a possible lack of ECO code
				if (eventDataCodes.size() == 0)
					eventDataCodes.add(1);

				if (locationCodes.size() == 1 && locationCodes.get(0).equals("0")) {
					NodeList listOfLCO = ifnElement.getElementsByTagName("LCO");

					for (int j = 0; j < listOfLCO.getLength(); j++) {
						NodeList locChildNodes = listOfLCO.item(j).getChildNodes();
						int xi = 0;
						int yi = 1;
						if (locChildNodes.getLength() == 4)
						{
							xi = 1;
							yi = 3;
						}

						double loc_lon = Float.parseFloat(locChildNodes.item(xi).getTextContent()) / 100000.0;
						double loc_lat = Float.parseFloat(locChildNodes.item(yi).getTextContent()) / 100000.0;

						locations.add(new Coordinate(loc_lon, loc_lat));
					}
				}

				List<String> msgText = ((ArrayList<String>) messageDataText.clone());

				if (locationCodes.size() == 1) {
					locationCodes.clear();
					double thresholdDistance = 10.0;

					for (Coordinate c : locations) {
						TmcSegment seg = tmcSegments.getClosestSegment(c, thresholdDistance);
						if (seg != null) {
							if (!locationCodes.contains(seg.getFrom()))
								locationCodes.add(seg.getFrom());
							if (!locationCodes.contains(seg.getTo()))
								locationCodes.add(seg.getTo());
						}
					}

					if (locationCodes.size() > 1) {
						List<Long> lcList = new ArrayList<Long>(locationCodes);
						List<Integer> evtList = new ArrayList<Integer>(eventDataCodes);

						TrafficMessageData m = new TrafficMessageData();
						m.messageDataText = msgText;
						m.locationCodes = lcList;
						m.eventDataCodes = evtList;
						m.direction = direction;
						m.bothDirections = bothDirections;
						m.startTime = startTime;
						m.endTime = endTime;
						m.uptTime = uptTime;
						m.expTime = expTime;

						messages.add(m);
					}
				} else {
					if ((locationCodes.size() > 0) && (eventDataCodes.size() > 0)) {
						if (direction == 1) {
							Collections.reverse(locationCodes);
						}

						List<Long> lcList = new ArrayList<Long>(locationCodes);
						List<Integer> evtList = new ArrayList<Integer>(eventDataCodes);

						for (int mi = 0; mi < lcList.size() - 1; mi++) {
							long sCode = lcList.get(mi);
							long eCode = lcList.get(mi + 1);

							List<Long> lcList1 = getCodeList(graph, sCode, eCode);

							TrafficMessageData m = new TrafficMessageData();
							m.messageDataText = msgText;
							m.locationCodes = lcList1;
							m.eventDataCodes = evtList;
							m.direction = direction;
							m.bothDirections = bothDirections;
							m.startTime = startTime;
							m.endTime = endTime;
							m.uptTime = uptTime;
							m.expTime = expTime;

							if (locationCodes.size() == 1 && locations.size() == 1) {
								m.location = locations.get(0);
							}

							messages.add(m);

							/*if (sCode != eCode && Math.abs(eCode - sCode) <= 4) {
								for (int ci = sCode; ci < eCode; ci++) {

									List<Integer> lcList1 = new ArrayList<Integer>();
									lcList1.add(ci);
									lcList1.add(ci + 1);

									TrafficMessageData m = new TrafficMessageData();
									m.messageDataText = msgText;
									m.locationCodes = lcList1;
									m.eventDataCodes = evtList;
									m.direction = direction;
									m.bothDirections = bothDirections;
									m.startTime = startTime;
									m.endTime = endTime;
									m.uptTime = uptTime;
									m.expTime = expTime;

									if (locationCodes.size() == 1 && locations.size() == 1) {
										m.location = locations.get(0);
									}

									messages.add(m);
								}
							} else {
								List<Integer> lcList1 = new ArrayList<Integer>();
								lcList1.add(sCode);
								if (sCode != eCode)
									lcList1.add(eCode);

								TrafficMessageData m = new TrafficMessageData();
								m.messageDataText = msgText;
								m.locationCodes = lcList1;
								m.eventDataCodes = evtList;
								m.direction = direction;
								m.bothDirections = bothDirections;
								m.startTime = startTime;
								m.endTime = endTime;
								m.uptTime = uptTime;
								m.expTime = expTime;

								if (locationCodes.size() == 1 && locations.size() == 1) {
									m.location = locations.get(0);
								}

								messages.add(m);
							}*/
						}
					}
				}
				messageDataText.clear();
				locationCodes.clear();
				eventDataCodes.clear();
				locations.clear();

				startTime = null;
				endTime = null;
				uptTime = null;
				expTime = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return messages;
	}

	private static List<Long> getCodeList(TrafficLocationGraph graph, long startIndex, long endIndex)
	{
		if (startIndex == endIndex)
		{
			List<Long> list = new ArrayList<>();
			list.add(startIndex);
			
			return list;
		}
		else
		{
			if (graph.containsCode(startIndex) && graph.containsCode(endIndex)&&graph.getShortestPath(startIndex, endIndex)!=null)
				return graph.getShortestPath(startIndex, endIndex);
			else
			{
				List<Long> list = new ArrayList<>();

				if (Math.abs(startIndex - endIndex) <= 5)
				{
					List<Long> lcList1 = new ArrayList<Long>();

					if (endIndex > startIndex)
					{
						for (long ci = startIndex; ci <= endIndex; ci++) {
							list.add(ci);
						}
					}
					else
					{
						for (long ci = endIndex; ci <= startIndex; ci++) {
							list.add(0, ci);
						}
					}
				}
				else
				{
					list.add(startIndex);
					if (startIndex != endIndex)
						list.add(endIndex);
				}

				return list;
			}			
		}
	}
}