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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class TmcEventCodesTable {

	private static HashMap<Integer, TrafficEventInfo> _dictCodes;
	
    static boolean isDelay = true;  // true if the message contains information of time delay, e.g., event code 1603
	static boolean isReverse = true;// true if the message indicates that the traffic flows is reversed (contraflow), e.g., event code 517. But now it is not used here, needs further investigations
	
	
	
	// This table is generated using TmcEventCodes.xls file.
	private static TrafficEventInfo[] CODES = new TrafficEventInfo[] {
			new TrafficEventInfo(1, "	traffic problem	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.SLOW_TRAFFIC),    // changed
			new TrafficEventInfo(2, "	queuing traffic (with average speeds Q). Danger of stationary traffic	", TrafficEventType.AVOID, 0.2f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(11, "	overheight warning system triggered	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(12, "	(Q) accident(s), traffic being directed around accident area	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(16, "	closed, rescue and recovery work in progress	", TrafficEventType.BLOCKED, 1f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(20, "	service area overcrowded, drive to another service area	", TrafficEventType.BLOCKED, 1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(22, "	service area, fuel station closed	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(23, "	service area, restaurant closed	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(24, "	bridge closed	", TrafficEventType.BLOCKED, 0f, TrafficEventCategory.COMPLETELY_CLOSED),  // changed by zwang
			new TrafficEventInfo(25, "	tunnel closed	", TrafficEventType.BLOCKED, 0f, TrafficEventCategory.COMPLETELY_CLOSED),  // changed by zwang
			new TrafficEventInfo(26, "	bridge blocked	", TrafficEventType.BLOCKED, 0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(27, "	tunnel blocked	", TrafficEventType.BLOCKED, 0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(28, "	road closed intermittently	", TrafficEventType.BLOCKED, 0f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(36, "	fuel station reopened	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(37, "	restaurant reopened	",  TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(39, "	reopening of bridge expected (Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), 
			new TrafficEventInfo(40, "	smog alert ended	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(41, "	(Q) overtaking lane(s) closed	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(42, "	(Q) overtaking lane(s) blocked	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(51, "	roadworks, (Q) overtaking lane(s) closed	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(52, "	(Q sets of) roadworks on the hard shoulder	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.UNDEFINED), // unclear
			new TrafficEventInfo(53, "	(Q sets of) roadworks in the emergency lane	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.UNDEFINED), // unclear
			new TrafficEventInfo(55, "	traffic problem expected	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(56, "	traffic congestion expected	",  TrafficEventType.AVOID, 0.2f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(57, "	normal traffic expected	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(61,
					"	(Q) object(s) on roadway {something that does not neccessarily block the road or part of it}	",
					 TrafficEventType.AVOID, 1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(62, "	(Q) burst pipe(s)	",  TrafficEventType.AVOID, 1f, TrafficEventCategory.ROADWORKS), // changed by zwang
			new TrafficEventInfo(63, "	(Q) object(s) on the road. Danger	", TrafficEventType.BLOCKED, 0.1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(64, "	burst pipe. Danger	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.ROADWORKS), // changed by zwang
			new TrafficEventInfo(70, "	traffic congestion, average speed of 10 km/h	", TrafficEventType.AVOID, 10f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(71, "	traffic congestion, average speed of 20 km/h	", TrafficEventType.AVOID, 20f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(72, "	traffic congestion, average speed of 30 km/h	", TrafficEventType.AVOID, 30f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(73, "	traffic congestion, average speed of 40 km/h	", TrafficEventType.AVOID, 40f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(74, "	traffic congestion, average speed of 50 km/h	", TrafficEventType.AVOID, 50f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(75, "	traffic congestion, average speed of 60 km/h	", TrafficEventType.AVOID, 60f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(76, "	traffic congestion, average speed of 70 km/h	", TrafficEventType.AVOID, 70f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(80, "	heavy traffic has to be expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(81, "	traffic congestion has to be expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(82, "	(Q sets of) roadworks. Heavy traffic has to be expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.ROADWORKS), // changed by zwang
			new TrafficEventInfo(83, "	closed ahead. Heavy traffic expected	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(84, "	major event. Heavy traffic has to be expected	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(85, "	sports meeting. Heavy traffic has to be expected	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(86, "	fair. Heavy traffic has to be expected	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(87, "	evacuation. Heavy traffic has to be expected	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(88, "	traffic congestion forecast withdrawn	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(89, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(91, "	delays (Q) for cars	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(101, "	stationary traffic	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(102, "	stationary traffic for 1 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(103, "	stationary traffic for 2 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(104, "	stationary traffic for 4 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(105, "	stationary traffic for 6 km	", TrafficEventType.AVOID, 0.50f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(106, "	stationary traffic for 10 km	", TrafficEventType.AVOID, 0.4f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(107, "	stationary traffic expected	", TrafficEventType.AVOID, 0.95f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(108, "	queuing traffic (with average speeds Q)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.SLOW_TRAFFIC),  // changed
			new TrafficEventInfo(109, "	queuing traffic for 1 km (with average speeds Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(110, "	queuing traffic for 2 km (with average speeds Q)	", TrafficEventType.AVOID, 0.75f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(111, "	queuing traffic for 4 km (with average speeds Q)	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(112, "	queuing traffic for 6 km (with average speeds Q)	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(113, "	queuing traffic for 10 km (with average speeds Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(114, "	queuing traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(115, "	slow traffic (with average speeds Q)	", TrafficEventType.AVOID, 0.95f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(116, "	slow traffic for 1 km (with average speeds Q)	", TrafficEventType.AVOID, 0.95f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(117, "	slow traffic for 2 km (with average speeds Q)	", TrafficEventType.AVOID, 0.90f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(118, "	slow traffic for 4 km (with average speeds Q)	", TrafficEventType.AVOID, 0.85f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(119, "	slow traffic for 6 km (with average speeds Q)	", TrafficEventType.AVOID, 0.85f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(120, "	slow traffic for 10 km (with average speeds Q)	", TrafficEventType.AVOID, 0.80f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(121, "	slow traffic expected	", TrafficEventType.AVOID, 0.80f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(122, "	heavy traffic (with average speeds Q)	", TrafficEventType.AVOID, 0.80f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(123, "	heavy traffic expected	", TrafficEventType.AVOID, 0.80f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(124, "	traffic flowing freely (with average speeds Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(125, "	traffic building up (with average speeds Q)	",  TrafficEventType.AVOID, 0.80f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(126, "	no problems to report	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(127, "	traffic congestion cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(128, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(129, "	stationary traffic for 3 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(130, "	danger of stationary traffic	", 0, 1f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(131, "	queuing traffic for 3 km (with average speeds Q)	", TrafficEventType.AVOID, 0.80f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(132, "	danger of queuing traffic (with average speeds Q)	", TrafficEventType.AVOID, 0.80f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(133, "	long queues (with average speeds Q)	", TrafficEventType.AVOID, 0.50f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(134, "	slow traffic for 3 km (with average speeds Q)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(135, "	traffic easing	",  TrafficEventType.AVOID, 0.90f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(136, "	traffic congestion (with average speeds Q)	",  TrafficEventType.AVOID, 0.80f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(137, "	traffic lighter than normal (with average speeds Q)	",  TrafficEventType.AVOID, 1.0f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(138, "	queuing traffic (with average speeds Q). Approach with care	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(139, "	queuing traffic around a bend in the road	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(140, "	queuing traffic over the crest of a hill	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(141, "	all accidents cleared, no problems to report	", TrafficEventType.ANY, 1.0f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(142, "	traffic heavier than normal (with average speeds Q)	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(143, "	traffic very much heavier than normal (with average speeds Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(200, "	multi vehicle pile up. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(201, "	(Q) accident(s)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(202, "	(Q) serious accident(s)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(203, "	multi-vehicle accident (involving Q vehicles)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(204, "	accident involving (a/Q) heavy lorr(y/ies)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(205, "	(Q) accident(s) involving hazardous materials	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(206, "	(Q) fuel spillage accident(s)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(207, "	(Q) chemical spillage accident(s)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(208, "	vehicles slowing to look at (Q) accident(s)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(209, "	(Q) accident(s) in the opposing lanes	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(210, "	(Q) shed load(s)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(211, "	(Q) broken down vehicle(s)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(212, "	(Q) broken down heavy lorr(y/ies)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(213, "	(Q) vehicle fire(s)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(214, "	(Q) incident(s)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(215, "	(Q) accident(s). Stationary traffic	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(216, "	(Q) accident(s). Stationary traffic for 1 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(217, "	(Q) accident(s). Stationary traffic for 2 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(218, "	(Q) accident(s). Stationary traffic for 4 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(219, "	(Q) accident(s). Stationary traffic for 6 km	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(220, "	(Q) accident(s). Stationary traffic for 10 km	", TrafficEventType.AVOID, 0.4f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(221, "	(Q) accident(s). Danger of stationary traffic	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(222, "	(Q) accident(s). Queuing traffic	", TrafficEventType.AVOID, 1f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(223, "	(Q) accident(s). Queuing traffic for 1 km	", TrafficEventType.AVOID, 0.99f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(224, "	(Q) accident(s). Queuing traffic for 2 km	", TrafficEventType.AVOID, 0.95f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(225, "	(Q) accident(s). Queuing traffic for 4 km	", TrafficEventType.AVOID, 0.92f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(226, "	(Q) accident(s). Queuing traffic for 6 km	", TrafficEventType.AVOID, 0.90f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(227, "	(Q) accident(s). Queuing traffic for 10 km	", TrafficEventType.AVOID, 0.85f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(228, "	(Q) accident(s). Danger of queuing traffic	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(229, "	(Q) accident(s). Slow traffic	", 0, 1f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(230, "	(Q) accident(s). Slow traffic for 1 km	", TrafficEventType.AVOID, 0.95f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(231, "	(Q) accident(s). Slow traffic for 2 km	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(232, "	(Q) accident(s). Slow traffic for 4 km	", TrafficEventType.AVOID, 0.85f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(233, "	(Q) accident(s). Slow traffic for 6 km	", TrafficEventType.AVOID, 0.80f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(234, "	(Q) accident(s). Slow traffic for 10 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(235, "	(Q) accident(s). Slow traffic expected	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(236, "	(Q) accident(s). Heavy traffic	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(237, "	(Q) accident(s). Heavy traffic expected	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(238, "	(Q) accident(s). Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(239, "	(Q) accident(s). Traffic building up	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(240, "	road closed due to (Q) accident(s)	",  TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(241, "	(Q) accident(s). Right lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(242, "	(Q) accident(s). Centre lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang,
			new TrafficEventInfo(243, "	(Q) accident(s). Left lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(244, "	(Q) accident(s). Hard shoulder blocked	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(245, "	(Q) accident(s). Two lanes blocked	", TrafficEventType.AVOID, 0.3f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(246, "	(Q) accident(s). Three lanes blocked	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(247, "	accident. Delays (Q)	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(248, "	accident. Delays (Q) expected	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(249, "	accident. Long delays (Q)	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(250, "	vehicles slowing to look at (Q) accident(s). Stationary traffic	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(251, "	vehicles slowing to look at (Q) accident(s). Stationary traffic for 1 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(252, "	vehicles slowing to look at (Q) accident(s). Stationary traffic for 2 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(253, "	vehicles slowing to look at (Q) accident(s). Stationary traffic for 4 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(254, "	vehicles slowing to look at (Q) accident(s). Stationary traffic for 6 km	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(255, "	vehicles slowing to look at (Q) accident(s). Stationary traffic for 10 km	", TrafficEventType.AVOID, 0.4f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(256, "	vehicles slowing to look at (Q) accident(s). Danger of stationary traffic	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(257, "	vehicles slowing to look at (Q) accident(s). Queuing traffic	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(258, "	vehicles slowing to look at (Q) accident(s). Queuing traffic for 1 km	",  TrafficEventType.AVOID, 0.75f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(259, "	vehicles slowing to look at (Q) accident(s). Queuing traffic for 2 km	",  TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(260, "	vehicles slowing to look at (Q) accident(s). Queuing traffic for 4 km	",  TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(261, "	vehicles slowing to look at (Q) accident(s). Queuing traffic for 6 km	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(262, "	vehicles slowing to look at (Q) accident(s). Queuing traffic for 10 km	",  TrafficEventType.AVOID, 0.55f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(263, "	vehicles slowing to look at (Q) accident(s). Danger of queuing traffic	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(264, "	vehicles slowing to look at (Q) accident(s). Slow traffic	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(265, "	vehicles slowing to look at (Q) accident(s). Slow traffic for 1 km	",  TrafficEventType.AVOID, 0.75f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(266, "	vehicles slowing to look at (Q) accident(s). Slow traffic for 2 km	",  TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(267, "	vehicles slowing to look at (Q) accident(s). Slow traffic for 4 km	",  TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(268, "	vehicles slowing to look at (Q) accident(s). Slow traffic for 6 km	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(269, "	vehicles slowing to look at (Q) accident(s). Slow traffic for 10 km	",  TrafficEventType.AVOID, 0.55f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(270, "	vehicles slowing to look at (Q) accident(s). Slow traffic expected	",  TrafficEventType.AVOID, 0.5f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(271, "	vehicles slowing to look at (Q) accident(s). Heavy traffic	",  TrafficEventType.AVOID, 0.5f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(272, "	vehicles slowing to look at (Q) accident(s). Heavy traffic expected	",  TrafficEventType.AVOID, 0.5f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(274, "	vehicles slowing to look at (Q) accident(s). Traffic building up	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(275, "	vehicles slowing to look at accident. Delays (Q)	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(276, "	vehicles slowing to look at accident. Delays (Q) expected	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(277, "	vehicles slowing to look at accident. Long delays (Q)	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(278, "	(Q) shed load(s). Stationary traffic	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(279, "	(Q) shed load(s). Stationary traffic for 1 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(280, "	(Q) shed load(s). Stationary traffic for 2 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(281, "	(Q) shed load(s). Stationary traffic for 4 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(282, "	(Q) shed load(s). Stationary traffic for 6 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(283, "	(Q) shed load(s). Stationary traffic for 10 km	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(284, "	(Q) shed load(s). Danger of stationary traffic	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(285, "	(Q) shed load(s). Queuing traffic	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(286, "	(Q) shed load(s). Queuing traffic for 1 km	", TrafficEventType.AVOID, 0.75f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(287, "	(Q) shed load(s). Queuing traffic for 2 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(288, "	(Q) shed load(s). Queuing traffic for 4 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(289, "	(Q) shed load(s). Queuing traffic for 6 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(290, "	(Q) shed load(s). Queuing traffic for 10 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(291, "	(Q) shed load(s). Danger of queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(292, "	(Q) shed load(s). Slow traffic	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(293, "	(Q) shed load(s). Slow traffic for 1 km	", TrafficEventType.AVOID, 0.75f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(294, "	(Q) shed load(s). Slow traffic for 2 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(295, "	(Q) shed load(s). Slow traffic for 4 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(296, "	(Q) shed load(s). Slow traffic for 6 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(297, "	(Q) shed load(s). Slow traffic for 10 km	",  TrafficEventType.AVOID, 0.55f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(298, "	(Q) shed load(s). Slow traffic expected	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(299, "	(Q) shed load(s). Heavy traffic	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(300, "	(Q) shed load(s). Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(301, "	(Q) shed load(s). Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(302, "	(Q) shed load(s). Traffic building up	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(303, "	blocked by (Q) shed load(s)	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(304, "	(Q) shed load(s). Right lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(305, "	(Q) shed load(s). Centre lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(306, "	(Q) shed load(s). Left lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(307, "	(Q) shed load(s). Hard shoulder blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(308, "	(Q) shed load(s). Two lanes blocked	", TrafficEventType.AVOID, 0.3f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(309, "	(Q) shed load(s). Three lanes blocked	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(310, "	shed load. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(311, "	shed load. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(312, "	shed load. Long delays (Q)	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(313, "	(Q) broken down vehicle(s). Stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(314, "	(Q) broken down vehicle(s). Danger of stationary traffic	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(315, "	(Q) broken down vehicle(s). Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(316, "	(Q) broken down vehicle(s). Danger of queuing traffic	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(317, "	(Q) broken down vehicle(s). Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(318, "	(Q) broken down vehicle(s). Slow traffic expected	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(319, "	(Q) broken down vehicle(s). Heavy traffic	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(320, "	(Q) broken down vehicle(s). Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(321, "	(Q) broken down vehicle(s). Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(322, "	(Q) broken down vehicle(s).Traffic building up	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(323, "	blocked by (Q) broken down vehicle(s).	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(324, "	(Q) broken down vehicle(s). Right lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(325, "	(Q) broken down vehicle(s). Centre lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang,
			new TrafficEventInfo(326, "	(Q) broken down vehicle(s). Left lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(327, "	(Q) broken down vehicle(s). Hard shoulder blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(328, "	(Q) broken down vehicle(s). Two lanes blocked	", TrafficEventType.AVOID, 0.3f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(329, "	(Q) broken down vehicle(s). Three lanes blocked	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(330, "	broken down vehicle. Delays (Q)	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(331, "	broken down vehicle. Delays (Q) expected	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(332, "	broken down vehicle. Long delays (Q)	", TrafficEventType.ANY, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(333, "	accident cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(334, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(335, "	accident involving (a/Q) bus(es)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(336, "	(Q) oil spillage accident(s)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(337, "	(Q) overturned vehicle(s)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(338, "	(Q) overturned heavy lorr(y/ies)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(339, "	(Q) jackknifed trailer(s)	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(340, "	(Q) jackknifed caravan(s)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(341, "	(Q) jackknifed articulated lorr(y/ies)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(342, "	(Q) vehicle(s) spun around	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(343, "	(Q) earlier accident(s)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(344, "	accident investigation work	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(345, "	(Q) secondary accident(s)	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(346, "	(Q) broken down bus(es)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(347, "	(Q) overheight vehicle(s)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(348, "	(Q) accident(s). Stationary traffic for 3 km	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(349, "	(Q) accident(s). Queuing traffic for 3 km	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(350, "	(Q) accident(s). Slow traffic for 3 km	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(351, "	(Q) accident(s) in roadworks area	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(352, "	vehicles slowing to look at (Q) accident(s). Stationary traffic for 3 km	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(353, "	vehicles slowing to look at (Q) accident(s). Queuing traffic for 3 km	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(354, "	vehicles slowing to look at (Q) accident(s). Slow traffic for 3 km	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(355, "	vehicles slowing to look at (Q) accident(s). Danger	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(356, "	(Q) shed load(s). Stationary traffic for 3 km	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(357, "	(Q) shed load(s). Queuing traffic for 3 km	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(358, "	(Q) shed load(s). Slow traffic for 3 km	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(359, "	(Q) shed load(s). Danger	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(360, "	(Q) overturned vehicle(s). Stationary traffic	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(361, "	(Q) overturned vehicle(s). Danger of stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(362, "	(Q) overturned vehicle(s). Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(363, "	(Q) overturned vehicle(s). Danger of queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(364, "	(Q) overturned vehicle(s). Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(365, "	(Q) overturned vehicle(s). Slow traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(366, "	(Q) overturned vehicle(s). Heavy traffic	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(367, "	(Q) overturned vehicle(s). Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(368, "	(Q) overturned vehicle(s). Traffic building up	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(369, "	blocked by (Q) overturned vehicle(s)	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(370, "	(Q) overturned vehicle(s). Right lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(371, "	(Q) overturned vehicle(s). Centre lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang,
			new TrafficEventInfo(372, "	(Q) overturned vehicle(s). Left lane blocked	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(373, "	(Q) overturned vehicle(s). Two lanes blocked	", TrafficEventType.AVOID, 0.3f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(374, "	(Q) overturned vehicle(s). Three lanes blocked	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(375, "	overturned vehicle. Delays (Q)	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(376, "	overturned vehicle. Delays (Q) expected	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(377, "	overturned vehicle. Long delays (Q)	", TrafficEventType.ANY, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(378, "	(Q) overturned vehicle(s). Danger	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(379, "	Stationary traffic due to (Q) earlier accident(s)	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(380, "	Danger of stationary traffic due to (Q) earlier accident(s)	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(381, "	Queuing traffic due to (Q) earlier accident(s)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(382, "	Danger of queuing traffic due to (Q) earlier accident(s)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(383, "	Slow traffic due to (Q) earlier accident(s)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(385, "	Heavy traffic due to (Q) earlier accident(s)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(387, "	Traffic building up due to (Q) earlier accident(s)	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(388, "	Delays (Q) due to earlier accident	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(390, "	Long delays (Q) due to earlier accident	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(391, "	accident investigation work. Danger	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(392, "	(Q) secondary accident(s). Danger	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(393, "	(Q) broken down vehicle(s). Danger	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(394, "	(Q) broken down heavy lorr(y/ies). Danger	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(395, "	road cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(396, "	incident cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(397, "	rescue and recovery work in progress	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(399, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(401, "	closed	", TrafficEventType.BLOCKED, 1f, TrafficEventCategory.COMPLETELY_CLOSED), //Changed
			new TrafficEventInfo(402, "	blocked	", TrafficEventType.BLOCKED, 1f, TrafficEventCategory.COMPLETELY_CLOSED), //Changed
			new TrafficEventInfo(403, "	closed for heavy vehicles (over Q)	", TrafficEventType.BLOCKED, 0.0f, TmcMode.HEAVY_VEHICLE, TrafficEventCategory.PARTIALLY_CLOSED), // changed by Zwang
			new TrafficEventInfo(404, "	no through traffic for heavy lorries (over Q)	",TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(405, "	no through traffic	",TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(406, "	(Q th) entry slip road closed	",TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(407, "	(Q th) exit slip road closed	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(408, "	slip roads closed	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(409, "	slip road restrictions	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(410, "	closed ahead. Stationary traffic	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(411, "	closed ahead. Stationary traffic for 1 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(412, "	closed ahead. Stationary traffic for 2 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(413, "	closed ahead. Stationary traffic for 4 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(414, "	closed ahead. Stationary traffic for 6 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(415, "	closed ahead. Stationary traffic for 10 km	", TrafficEventType.AVOID, 0.4f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(416, "	closed ahead. Danger of stationary traffic	", TrafficEventType.ANY, 0.95f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(417, "	closed ahead. Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(418, "	closed ahead. Queuing traffic for 1 km	", TrafficEventType.AVOID, 0.75f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(419, "	closed ahead. Queuing traffic for 2 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(420, "	closed ahead. Queuing traffic for 4 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(421, "	closed ahead. Queuing traffic for 6 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(422, "	closed ahead. Queuing traffic for 10 km	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(423, "	closed ahead. Danger of queuing traffic	",  TrafficEventType.ANY, 0.95f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(424, "	closed ahead. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(425, "	closed ahead. Slow traffic for 1 km	", TrafficEventType.AVOID, 0.75f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(426, "	closed ahead. Slow traffic for 2 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(427, "	closed ahead. Slow traffic for 4 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(428, "	closed ahead. Slow traffic for 6 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(429, "	closed ahead. Slow traffic for 10 km	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(430, "	closed ahead. Slow traffic expected	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(431, "	closed ahead. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(432, "	closed ahead. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(433, "	closed ahead. Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(434, "	closed ahead. Traffic building up	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(435, "	closed ahead. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(436, "	closed ahead. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(437, "	closed ahead. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(438, "	blocked ahead. Stationary traffic	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(439, "	blocked ahead. Stationary traffic for 1 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(440, "	blocked ahead. Stationary traffic for 2 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(441, "	blocked ahead. Stationary traffic for 4 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(442, "	blocked ahead. Stationary traffic for 6 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(443, "	blocked ahead. Stationary traffic for 10 km	", TrafficEventType.AVOID, 0.4f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(444, "	blocked ahead. Danger of stationary traffic	", TrafficEventType.ANY, 0.7f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(445, "	blocked ahead. Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(446, "	blocked ahead. Queuing traffic for 1 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(447, "	blocked ahead. Queuing traffic for 2 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(448, "	blocked ahead. Queuing traffic for 4 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(449, "	blocked ahead. Queuing traffic for 6 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(450, "	blocked ahead. Queuing traffic for 10 km	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(451, "	blocked ahead. Danger of queuing traffic	",  TrafficEventType.ANY, 0.95f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(452, "	blocked ahead. Slow traffic	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(453, "	blocked ahead. Slow traffic for 1 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(454, "	blocked ahead. Slow traffic for 2 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(455, "	blocked ahead. Slow traffic for 4 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(456, "	blocked ahead. Slow traffic for 6 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(457, "	blocked ahead. Slow traffic for 10 km	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(458, "	blocked ahead. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(459, "	blocked ahead. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(460, "	blocked ahead. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(461, "	blocked ahead. Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(462, "	blocked ahead. Traffic building up	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(463, "	blocked ahead. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(464, "	blocked ahead. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(465, "	blocked ahead. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(466, "	slip roads reopened	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(467, "	reopened	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(468, "	message cancelled	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(469, "	closed ahead	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(470, "	blocked ahead	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(471, "	(Q) entry slip road(s) closed	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(472, "	(Q th) entry slip road blocked	",TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(473, "	entry blocked	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(474, "	(Q) exit slip road(s) closed	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(475, "	(Q th) exit slip road blocked	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(476, "	exit blocked	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(477, "	slip roads blocked	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(478, "	connecting carriageway closed	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(479, "	parallel carriageway closed	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(480, "	right-hand parallel carriageway closed	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(481, "	left-hand parallel carriageway closed	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(482, "	express lanes closed	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(483, "	through traffic lanes closed	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(484, "	local lanes closed	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(485, "	connecting carriageway blocked	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(486, "	parallel carriageway blocked	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(487, "	right-hand parallel carriageway blocked	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(488, "	left-hand parallel carriageway blocked	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(489, "	express lanes blocked	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(490, "	through traffic lanes blocked	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(491, "	local lanes blocked	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(492, "	no motor vehicles	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(493, "	restrictions	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(494, "	closed for heavy lorries (over Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(495, "	closed ahead. Stationary traffic for 3 km	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(496, "	closed ahead. Queuing traffic for 3 km	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(497, "	closed ahead. Slow traffic for 3 km	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(498, "	blocked ahead. Stationary traffic for 3 km	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(499, "	blocked ahead. Queuing traffic for 3 km	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(500, "	(Q) lane(s) closed	", 0, 1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed
			new TrafficEventInfo(501, "	(Q) right lane(s) closed	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.PARTIALLY_CLOSED), //Changed
			new TrafficEventInfo(502, "	(Q) centre lane(s) closed	", 0, 1f, TrafficEventCategory.PARTIALLY_CLOSED),
			new TrafficEventInfo(503, "	(Q) left lane(s) closed	", 0, 1f, TrafficEventCategory.PARTIALLY_CLOSED),
			new TrafficEventInfo(504, "	hard shoulder closed	", 0, 1f, TrafficEventCategory.PARTIALLY_CLOSED),// changed
			new TrafficEventInfo(505, "	two lanes closed	", 0, 1f, TrafficEventCategory.PARTIALLY_CLOSED),// changed
			new TrafficEventInfo(506, "	three lanes closed	", 0, 1f, TrafficEventCategory.PARTIALLY_CLOSED),// changed
			new TrafficEventInfo(507, "	(Q) right lane(s) blocked	", 0, 1f, TrafficEventCategory.PARTIALLY_CLOSED),// changed
			new TrafficEventInfo(508, "	(Q) centre lane(s) blocked	", 0, 1f, TrafficEventCategory.PARTIALLY_CLOSED),// changed
			new TrafficEventInfo(509, "	(Q) left lane(s) blocked	", 0, 1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed
			new TrafficEventInfo(510, "	hard shoulder blocked	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(511, "	two lanes blocked	", 0, 1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed
			new TrafficEventInfo(512, "	three lanes blocked	", 0, 1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed
			new TrafficEventInfo(513, "	single alternate line traffic	", TrafficEventType.AVOID, 0.95f, TrafficEventCategory.PARTIALLY_CLOSED), // changed
			new TrafficEventInfo(514, "	carriageway reduced (from Q lanes) to one lane	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED),   //Changed         
			new TrafficEventInfo(515, "	carriageway reduced (from Q lanes) to two lanes	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.PARTIALLY_CLOSED),  //Changed 
			new TrafficEventInfo(516, "	carriageway reduced (from Q lanes) to three lanes	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.PARTIALLY_CLOSED),  //Changed 
			new TrafficEventInfo(517, "	contraflow	", TrafficEventType.AVOID, 0.8f, isReverse, TrafficEventCategory.WARNING), //Changed by zwang
			new TrafficEventInfo(518, "	narrow lanes	", 0, 1f, TrafficEventCategory.WARNING),  //Changed
			new TrafficEventInfo(519, "	contraflow with narrow lanes	",TrafficEventType.AVOID, 0.8f, isReverse, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(520, "	(Q) lane(s) blocked	", 0, 1f, TrafficEventCategory.PARTIALLY_CLOSED),
			new TrafficEventInfo(521, "	(Q) lanes closed. Stationary traffic	", 0, 1f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(522, "	(Q) lanes closed. Stationary traffic for 1 km	", 0, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(523, "	(Q) lanes closed. Stationary traffic for 2 km	", 0, 0.75f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(524, "	(Q) lanes closed. Stationary traffic for 4 km	", 0, 7f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(525, "	(Q) lanes closed. Stationary traffic for 6 km	", 0, 65f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(526, "	(Q) lanes closed. Stationary traffic for 10 km	",0, 6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(527, "	(Q) lanes closed. Danger of stationary traffic	", TrafficEventType.ANY, 0.95f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(528, "	(Q) lanes closed. Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(529, "	(Q) lanes closed. Queuing traffic for 1 km	", TrafficEventType.AVOID, 0.75f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(530, "	(Q) lanes closed. Queuing traffic for 2 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(531, "	(Q) lanes closed. Queuing traffic for 4 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(532, "	(Q) lanes closed. Queuing traffic for 6 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(533, "	(Q) lanes closed. Queuing traffic for 10 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(534, "	(Q) lanes closed. Danger of queuing traffic	", TrafficEventType.ANY, 0.95f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(535, "	(Q) lanes closed. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(536, "	(Q) lanes closed. Slow traffic for 1 km	", TrafficEventType.AVOID, 0.75f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(537, "	(Q) lanes closed. Slow traffic for 2 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(538, "	(Q) lanes closed. Slow traffic for 4 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(539, "	(Q) lanes closed. Slow traffic for 6 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(540, "	(Q) lanes closed. Slow traffic for 10 km	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(541, "	(Q) lanes closed. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(542, "	(Q) lanes closed. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(543, "	(Q) lanes closed. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(544, "	(Q)lanes closed. Traffic flowing freely	",TrafficEventType.AVOID, 0.9f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(545, "	(Q)lanes closed. Traffic building up	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(546, "	carriageway reduced (from Q lanes) to one lane. Stationary traffic	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(547, "	carriageway reduced (from Q lanes) to one lane. Danger of stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(548, "	carriageway reduced (from Q lanes) to one lane. Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(549, "	carriageway reduced (from Q lanes) to one lane. Danger of queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(550, "	carriageway reduced (from Q lanes) to one lane. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(551, "	carriageway reduced (from Q lanes) to one lane. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(552, "	carriageway reduced (from Q lanes) to one lane. Heavy traffic	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(553, "	carriageway reduced (from Q lanes) to one lane. Heavy traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(554, "	carriageway reduced (from Q lanes) to one lane. Traffic flowing freely	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(555, "	carriageway reduced (from Q lanes) to one lane. Traffic building up	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(556, "	carriageway reduced (from Q lanes) to two lanes. Stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(557, "	carriageway reduced (from Q lanes) to two lanes. Danger of stationary traffic	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(558, "	carriageway reduced (from Q lanes) to two lanes. Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(559, "	carriageway reduced (from Q lanes) to two lanes. Danger of queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(560, "	carriageway reduced (from Q lanes) to two lanes. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(561, "	carriageway reduced (from Q lanes) to two lanes. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(562, "	carriageway reduced (from Q lanes) to two lanes. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(563, "	carriageway reduced (from Q lanes) to two lanes. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(564, "	carriageway reduced (from Q lanes) to two lanes. Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(565, "	carriageway reduced (from Q lanes) to two lanes. Traffic building up	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(566, "	carriageway reduced (from Q lanes) to three lanes. Stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(567, "	carriageway reduced (from Q lanes) to three lanes. Danger of stationary traffic	",
					TrafficEventType.ANY, 0.95f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(568, "	carriageway reduced (from Q lanes) to three lanes. Queuing traffic	", 0, 1f),
			new TrafficEventInfo(569, "	carriageway reduced (from Q lanes) to three lanes. Danger of queuing traffic	", TrafficEventType.ANY, 0.95f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(570, "	carriageway reduced (from Q lanes) to three lanes. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(571, "	carriageway reduced (from Q lanes) to three lanes. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(572, "	carriageway reduced (from Q lanes) to three lanes. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(573, "	carriageway reduced (from Q lanes) to three lanes. Heavy traffic expected	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(574, "	carriageway reduced (from Q lanes) to three lanes. Traffic flowing freely	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(575, "	carriageway reduced (from Q lanes) to three lanes. Traffic building up	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(576, "	contraflow. Stationary traffic	", TrafficEventType.AVOID, 0.8f, isReverse, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(577, "	contraflow. Stationary traffic for 1 km	", TrafficEventType.AVOID, 0.7f, isReverse, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(578, "	contraflow. Stationary traffic for 2 km	", TrafficEventType.AVOID, 0.65f, isReverse, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(579, "	contraflow. Stationary traffic for 4 km	",TrafficEventType.AVOID, 0.6f, isReverse, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(580, "	contraflow. Stationary traffic for 6 km	", TrafficEventType.AVOID, 0.55f, isReverse, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(581, "	contraflow. Stationary traffic for 10 km	",TrafficEventType.AVOID, 0.5f, isReverse, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(582, "	contraflow. Danger of stationary traffic	", TrafficEventType.ANY, 0.95f, isReverse, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(583, "	contraflow. Queuing traffic	", TrafficEventType.AVOID, 0.8f, isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(584, "	contraflow. Queuing traffic for 1 km	", TrafficEventType.AVOID, 0.7f, isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(585, "	contraflow. Queuing traffic for 2 km	", TrafficEventType.AVOID, 0.65f, isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(586, "	contraflow. Queuing traffic for 4 km	", TrafficEventType.AVOID, 0.6f, isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(587, "	contraflow. Queuing traffic for 6 km	", TrafficEventType.AVOID, 0.55f,isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(588, "	contraflow. Queuing traffic for 10 km	", TrafficEventType.AVOID, 0.5f, isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(589, "	contraflow. Danger of queuing traffic	", TrafficEventType.ANY, 0.95f, isReverse, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(590, "	contraflow. Slow traffic	", TrafficEventType.AVOID, 0.8f, isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(591, "	contraflow. Slow traffic for 1 km	", TrafficEventType.AVOID, 0.7f, isReverse,TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(592, "	contraflow. Slow traffic for 2 km	", TrafficEventType.AVOID, 0.65f, isReverse,TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(593, "	contraflow. Slow traffic for 4 km	", TrafficEventType.AVOID, 0.6f, isReverse,TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(594, "	contraflow. Slow traffic for 6 km	",TrafficEventType.AVOID, 0.55f, isReverse,TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(595, "	contraflow. Slow traffic for 10 km	",TrafficEventType.AVOID, 0.5f, isReverse,TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(596, "	contraflow. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, isReverse,TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(597, "	contraflow. Heavy traffic	", TrafficEventType.AVOID, 0.6f, isReverse,TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(598, "	contraflow. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, isReverse,TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(599, "	contraflow. Traffic flowing freely	",TrafficEventType.AVOID, 1.0f,isReverse, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(600, "	contraflow. Traffic building up	", TrafficEventType.AVOID, 0.8f, isReverse,TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(601, "	contraflow. Carriageway reduced (from Q lanes) to one lane	", TrafficEventType.AVOID, 0.8f, isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(602, "	contraflow. Carriageway reduced (from Q lanes) to two lanes	", TrafficEventType.AVOID, 0.4f,isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(603, "	contraflow. Carriageway reduced (from Q lanes) to three lanes	", TrafficEventType.AVOID, 0.1f,isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(604, "	narrow lanes. Stationary traffic	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(605, "	narrow lanes. Danger of stationary traffic	", TrafficEventType.ANY, 0.95f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(606, "	narrow lanes. Queuing traffic	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(607, "	narrow lanes. Danger of queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(608, "	narrow lanes. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(609, "	narrow lanes. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(610, "	narrow lanes. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(611, "	narrow lanes. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(612, "	narrow lanes. Traffic flowing freely	", TrafficEventType.AVOID, 1.0f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(613, "	narrow lanes. Traffic building up	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(614, "	contraflow with narrow lanes. Stationary traffic	", TrafficEventType.AVOID, 0.8f, isReverse,TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(615, "	contraflow with narrow lanes. Stationary traffic. Danger of stationary traffic	",
					TrafficEventType.AVOID, 0.8f, isReverse, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(616, "	contraflow with narrow lanes. Queuing traffic	", TrafficEventType.AVOID, 0.8f, isReverse,TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(617, "	contraflow with narrow lanes. Danger of queuing traffic	", TrafficEventType.AVOID, 0.8f, isReverse, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(618, "	contraflow with narrow lanes. Slow traffic	", TrafficEventType.AVOID, 0.8f, isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(619, "	contraflow with narrow lanes. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(620, "	contraflow with narrow lanes. Heavy traffic	", TrafficEventType.AVOID, 0.6f, isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(621, "	contraflow with narrow lanes. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, isReverse, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(622, "	contraflow with narrow lanes. Traffic flowing freely	", TrafficEventType.AVOID, 1f,isReverse, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(623, "	contraflow with narrow lanes. Traffic building up	", TrafficEventType.AVOID, 0.8f, isReverse,TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(624, "	lane closures removed	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(625, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(626, "	blocked ahead. Slow traffic for 3 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(627, "	no motor vehicles without catalytic converters	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(628, "	no motor vehicles with even-numbered registration plates	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(629, "	no motor vehicles with odd-numbered registration plates	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(630, "	open	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(631, "	road cleared	",TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(632, "	entry reopened	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(633, "	exit reopened	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(634, "	all carriageways reopened	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(635, "	motor vehicle restrictions lifted	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(636, "	traffic restrictions lifted {reopened for all traffic}	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(637, "	emergency lane closed	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(638, "	turning lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(639, "	crawler lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(640, "	slow vehicle lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(641, "	one lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(642, "	emergency lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(643, "	turning lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(644, "	crawler lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(645, "	slow vehicle lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(646, "	one lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(647, "	(Q person) carpool lane in operation	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(648, "	(Q person) carpool lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(649, "	(Q person) carpool lane blocked	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(650, "	carpool restrictions changed (to Q persons per vehicle)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(651, "	(Q) lanes closed. Stationary traffic for 3 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(652, "	(Q) lanes closed. Queuing traffic for 3 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(653, "	(Q) lanes closed. Slow traffic for 3 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(654, "	contraflow. Stationary traffic for 3 km	", TrafficEventType.AVOID, 0.8f, isReverse, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(655, "	contraflow. Queuing traffic for 3 km	", TrafficEventType.AVOID, 0.8f, isReverse,TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(656, "	contraflow. Slow traffic for 3 km	", TrafficEventType.AVOID, 0.8f, isReverse,TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(657, "	lane blockages cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(658, "	contraflow removed	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(659, "	(Q person) carpool restrictions lifted	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(660, "	lane restrictions lifted	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(661, "	use of hard shoulder allowed	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang
			new TrafficEventInfo(662, "	normal lane regulations restored	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(663, "	all carriageways cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(664, "	carriageway closed	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(665, "	both directions closed	", TrafficEventType.BLOCKED, 0.1f, TrafficEventCategory.COMPLETELY_CLOSED), // changed
			new TrafficEventInfo(666, "	intermittent short term closures	", TrafficEventType.BLOCKED, 0.8f, TrafficEventCategory.COMPLETELY_CLOSED), // changed
			new TrafficEventInfo(671, "	bus lane available for carpools (with at least Q occupants)	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(672, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(673, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(675, "	(Q) salting vehicles	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(676, "	bus lane blocked	", TrafficEventType.BLOCKED, 0.0f, TmcMode.BUS, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(678, "	heavy vehicle lane closed	", TrafficEventType.BLOCKED, 0.0f, TmcMode.HEAVY_VEHICLE, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang,
			new TrafficEventInfo(679, "	heavy vehicle lane blocked	", TrafficEventType.BLOCKED, 0.0f, TmcMode.HEAVY_VEHICLE, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang,
			new TrafficEventInfo(680, "	reopened for through traffic	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(681, "	(Q) snowploughs	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang,
			new TrafficEventInfo(701, "	(Q sets of) roadworks	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.ROADWORKS), //changed
			new TrafficEventInfo(702, "	(Q sets of) major roadworks	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(703, "	(Q sets of) maintenance work	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(704, "	(Q sections of) resurfacing work	", TrafficEventType.BLOCKED, 0f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(705, "	(Q sets of) central reservation work	", TrafficEventType.BLOCKED, 0f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(706, "	(Q sets of) road marking work	", TrafficEventType.BLOCKED, 0f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(707, "	bridge maintenance work (at Q bridges)	", TrafficEventType.AVOID, 0.95f, TrafficEventCategory.ROADWORKS), // changed
			new TrafficEventInfo(708, "	(Q sets of) temporary traffic lights	", 0, 1f), // changed
			new TrafficEventInfo(709, "	(Q sections of) blasting work	", TrafficEventType.BLOCKED, 0f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(710, "	(Q sets of) roadworks. Stationary traffic	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(711, "	(Q sets of) roadworks. Stationary traffic for 1 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(712, "	(Q sets of) roadworks. Stationary traffic for 2 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(713, "	(Q sets of) roadworks. Stationary traffic for 4 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(714, "	(Q sets of) roadworks. Stationary traffic for 6 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(715, "	(Q sets of) roadworks. Stationary traffic for 10 km	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed
			new TrafficEventInfo(716, "	(Q sets of) roadworks. Danger of stationary traffic	", TrafficEventType.ANY, 0.95f, TrafficEventCategory.WARNING),  // changed
			new TrafficEventInfo(717, "	(Q sets of) roadworks. Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(718, "	(Q sets of) roadworks. Queuing traffic for 1 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(719, "	(Q sets of) roadworks. Queuing traffic for 2 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(720, "	(Q sets of) roadworks. Queuing traffic for 4 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(721, "	(Q sets of) roadworks. Queuing traffic for 6 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(722, "	(Q sets of) roadworks. Queuing traffic for 10 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(723, "	(Q sets of) roadworks. Danger of queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(724, "	(Q sets of) roadworks. Slow traffic	", 0, 0.95f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(725, "	(Q sets of) roadworks. Slow traffic for 1 km	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(726, "	(Q sets of) roadworks. Slow traffic for 2 km	", TrafficEventType.AVOID, 0.85f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(727, "	(Q sets of) roadworks. Slow traffic for 4 km	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(728, "	(Q sets of) roadworks. Slow traffic for 6 km	", TrafficEventType.AVOID, 0.75f, TrafficEventCategory.SLOW_TRAFFIC), // changed,
			new TrafficEventInfo(729, "	(Q sets of) roadworks. Slow traffic for 10 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed
			new TrafficEventInfo(730, "	(Q sets of) roadworks. Slow traffic expected	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(731, "	(Q sets of) roadworks. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(732, "	(Q sets of) roadworks. Heavy traffic expected	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(733, "	(Q sets of) roadworks. Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(734, "	(Q sets of) roadworks. Traffic building up	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(735, "	closed due to (Q sets of) roadworks	", TrafficEventType.BLOCKED, 1f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(736, "	(Q sets of) roadworks. Right lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(737, "	(Q sets of) roadworks. Centre lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(738, "	(Q sets of) roadworks. Left lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(739, "	(Q sets of) roadworks. Hard shoulder closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(740, "	(Q sets of) roadworks. Two lanes closed	",TrafficEventType.AVOID, 0.3f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(741, "	(Q sets of) roadworks. Three lanes closed	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(742, "	(Q sets of) roadworks. Single alternate line traffic	", TrafficEventType.AVOID, 0.3f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(743, "	roadworks. Carriageway reduced (from Q lanes) to one lane	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(744, "	roadworks. Carriageway reduced (from Q lanes) to two lanes	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(745, "	roadworks. Carriageway reduced (from Q lanes) to three lanes	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(746, "	(Q sets of) roadworks. Contraflow	", TrafficEventType.ANY, 1f, isReverse, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(747, "	roadworks. Delays (Q)	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(748, "	roadworks. Delays (Q) expected	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(749, "	roadworks. Long delays (Q)	", TrafficEventType.ANY, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(750, "	(Q sections of) resurfacing work. Stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(751, "	(Q sections of) resurfacing work. Stationary traffic for 1 km	",TrafficEventType.AVOID, 0.7f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(752, "	(Q sections of) resurfacing work. Stationary traffic for 2 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(753, "	(Q sections of) resurfacing work. Stationary traffic for 4 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(754, "	(Q sections of) resurfacing work. Stationary traffic for 6 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(755, "	(Q sections of) resurfacing work. Stationary traffic for 10 km	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(756, "	(Q sections of) resurfacing work. Danger of stationary traffic	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(757, "	(Q sections of) resurfacing work. Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(758, "	(Q sections of) resurfacing work. Queuing traffic for 1 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(759, "	(Q sections of) resurfacing work. Queuing traffic for 2 km	", TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(760, "	(Q sections of) resurfacing work. Queuing traffic for 4 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(761, "	(Q sections of) resurfacing work. Queuing traffic for 6 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(762, "	(Q sections of) resurfacing work. Queuing traffic for 10 km	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(763, "	(Q sections of) resurfacing work. Danger of queuing traffic	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(764, "	(Q sections of) resurfacing work. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(765, "	(Q sections of) resurfacing work. Slow traffic for 1 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(766, "	(Q sections of) resurfacing work. Slow traffic for 2 km	",TrafficEventType.AVOID, 0.65f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(767, "	(Q sections of) resurfacing work. Slow traffic for 4 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(768, "	(Q sections of) resurfacing work. Slow traffic for 6 km	", TrafficEventType.AVOID, 0.55f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(769, "	(Q sections of) resurfacing work. Slow traffic for 10 km	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(770, "	(Q sections of) resurfacing work. Slow traffic expected	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(771, "	(Q sections of) resurfacing work. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(772, "	(Q sections of) resurfacing work. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(773, "	(Q sections of) resurfacing work. Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(774, "	(Q sections of) resurfacing work. Traffic building up	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(775, "	(Q sections of) resurfacing work. Single alternate line traffic	", TrafficEventType.ANY, 0.3f, TrafficEventCategory.ROADWORKS), // changed by zwang
			new TrafficEventInfo(776, "	resurfacing work. Carriageway reduced (from Q lanes) to one lane	",TrafficEventType.AVOID, 0.1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(777, "	resurfacing work. Carriageway reduced (from Q lanes) to two lanes	", TrafficEventType.AVOID, 0.3f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(778, "	resurfacing work. Carriageway reduced (from Q lanes) to three lanes	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang,
			new TrafficEventInfo(779, "	(Q sections of) resurfacing work. Contraflow	",  TrafficEventType.AVOID, 1f, isReverse, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(780, "	resurfacing work. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(781, "	resurfacing work. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(782, "	resurfacing work. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(783, "	(Q sets of) road marking work. Stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(784, "	(Q sets of) road marking work. Danger of stationary traffic	", TrafficEventType.ANY, 0.6f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(785, "	(Q sets of) road marking work. Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(786, "	(Q sets of) road marking work. Danger of queuing traffic	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(787, "	(Q sets of) road marking work. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(788, "	(Q sets of) road marking work. Slow traffic expected	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(789, "	(Q sets of) road marking work. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(790, "	(Q sets of) road marking work. Heavy traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(791, "	(Q sets of) road marking work. Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(792, "	(Q sets of) road marking work. Traffic building up	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(793, "	(Q sets of) road marking work. Right lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(794, "	(Q sets of) road marking work. Centre lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(795, "	(Q sets of) road marking work. Left lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(796, "	(Q sets of) road marking work. Hard shoulder closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(797, "	(Q sets of) road marking work. Two lanes closed	", TrafficEventType.AVOID, 0.3f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(798, "	(Q sets of) road marking work. Three lanes closed	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(799, "	closed for bridge demolition work (at Q bridges)	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(800, "	roadworks cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(801, "	message cancelled	", 0, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed
			new TrafficEventInfo(802, "	(Q sets of) long-term roadworks	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.ROADWORKS), // changed
			new TrafficEventInfo(803, "	(Q sets of) construction work	", TrafficEventType.ANY, 1f, TrafficEventCategory.ROADWORKS),  // changed
			new TrafficEventInfo(804, "	(Q sets of) slow moving maintenance vehicles	", TrafficEventType.AVOID, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(805, "	bridge demolition work (at Q bridges)	",  TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(806, "	(Q sets of) water main work	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(807, "	(Q sets of) gas main work	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(808, "	(Q sets of) work on buried cables	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(809, "	(Q sets of) work on buried services	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(810, "	new roadworks layout	", TrafficEventType.ANY, 1f, TrafficEventCategory.ROADWORKS), // changed by zwang
			new TrafficEventInfo(811, "	new road layout	", TrafficEventType.ANY, 1f, TrafficEventCategory.ROADWORKS), // changed by zwang
			new TrafficEventInfo(812, "	(Q sets of) roadworks. Stationary traffic for 3 km	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.ROADWORKS), // changed
			new TrafficEventInfo(813, "	(Q sets of) roadworks. Queuing traffic for 3 km	", 0, 1f, TrafficEventCategory.ROADWORKS),
			new TrafficEventInfo(814, "	(Q sets of) roadworks. Slow traffic for 3 km	", TrafficEventType.AVOID, 0.85f, TrafficEventCategory.ROADWORKS), // changed
			new TrafficEventInfo(815, "	(Q sets of) roadworks during the day time	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.ROADWORKS), // changed
			new TrafficEventInfo(816, "	(Q sets of) roadworks during off-peak periods	", TrafficEventType.AVOID, 0.95f, TrafficEventCategory.ROADWORKS), // changed
			new TrafficEventInfo(817, "	(Q sets of) roadworks during the night	",  TrafficEventType.ANY, 1f, TrafficEventCategory.ROADWORKS), // changed by zwang
			new TrafficEventInfo(818, "	(Q sections of) resurfacing work. Stationary traffic for 3 km	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(819, "	(Q sections of) resurfacing work. Queuing traffic for 3 km	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(820, "	(Q sections of) resurfacing work. Slow traffic for 3 km	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(821, "	(Q sets of) resurfacing work during the day time	",  TrafficEventType.ANY, 1f, TrafficEventCategory.ROADWORKS), // changed by zwang
			new TrafficEventInfo(822, "	(Q sets of) resurfacing work during off-peak periods	",  TrafficEventType.ANY, 1f, TrafficEventCategory.ROADWORKS), // changed by zwang
			new TrafficEventInfo(823, "	(Q sets of) resurfacing work during the night	", TrafficEventType.ANY, 1f, TrafficEventCategory.ROADWORKS), // changed by zwang
			new TrafficEventInfo(824, "	(Q sets of) road marking work. Danger	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(825, "	(Q sets of) slow moving maintenance vehicles. Stationary traffic	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(826, "	(Q sets of) slow moving maintenance vehicles. Danger of stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(827, "	(Q sets of) slow moving maintenance vehicles. Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(828, "	(Q sets of) slow moving maintenance vehicles. Danger of queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(829, "	(Q sets of) slow moving maintenance vehicles. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(830, "	(Q sets of) slow moving maintenance vehicles. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(831, "	(Q sets of) slow moving maintenance vehicles. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(832, "	(Q sets of) slow moving maintenance vehicles. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(833, "	(Q sets of) slow moving maintenance vehicles. Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(834, "	(Q sets of) slow moving maintenance vehicles. Traffic building up	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(835, "	(Q sets of) slow moving maintenance vehicles. Right lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(836, "	(Q sets of) slow moving maintenance vehicles. Centre lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(837, "	(Q sets of) slow moving maintenance vehicles. Left lane closed	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(838, "	(Q sets of) slow moving maintenance vehicles. Two lanes closed	",TrafficEventType.AVOID, 0.3f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(839, "	(Q sets of) slow moving maintenance vehicles. Three lanes closed	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(840, "	water main work. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(841, "	water main work. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(842, "	water main work. Long delays (Q)	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(843, "	gas main work. Delays (Q)	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(844, "	gas main work. Delays (Q) expected	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(845, "	gas main work. Long delays (Q)	", TrafficEventType.ANY, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(846, "	work on buried cables. Delays (Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(847, "	work on buried cables. Delays (Q) expected	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(848, "	work on buried cables. Long delays (Q)	", TrafficEventType.ANY, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(849, "	work on buried services. Delays (Q)	",TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(850, "	work on buried services. Delays (Q) expected	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(851, "	work on buried services. Long delays (Q)	", TrafficEventType.ANY, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(852, "	construction traffic merging	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(853, "	roadwork clearance in progress	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(854, "	maintenance work cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(855, "	road layout unchanged	",TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(856, "	construction traffic merging. Danger	", TrafficEventType.ANY, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(857, "	(Q) unprotected accident area(s)	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang,
			new TrafficEventInfo(858, "	danger of(Q) unprotected accident area(s)	", 0, 1f, TrafficEventCategory.UNDEFINED), // changed
			new TrafficEventInfo(859, "	(Q) unlit vehicle(s) on the road	",  TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang
			new TrafficEventInfo(860, "	danger of (Q) unlit vehicle(s) on the road	",  TrafficEventType.BLOCKED, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(861, "	snow and ice debris	",  TrafficEventType.AVOID, 0.5f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(862, "	danger of snow and ice debris	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(897, "	people throwing objects onto the road. Danger	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(898, "	obstruction warning withdrawn	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(899, "	clearance work in progress, road free again	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(900, "	flooding expected	",  TrafficEventType.BLOCKED, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(901, "	(Q) obstruction(s) on roadway {something that does block the road or part of it}	",
					TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang 
			new TrafficEventInfo(902, "	(Q) obstructions on the road. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(903, "	spillage on the road	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(904, "	storm damage	",  TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(905, "	(Q) fallen trees	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(906, "	(Q) fallen trees. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(907, "	flooding	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(908, "	flooding. Danger	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(909, "	flash floods	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(910, "	danger of flash floods	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(911, "	avalanches	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(912, "	avalanche risk	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(913, "	rockfalls	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(914, "	landslips	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(915, "	earthquake damage	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(916, "	road surface in poor condition	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(917, "	subsidence	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(918, "	(Q) collapsed sewer(s)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(919, "	burst water main	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(920, "	gas leak	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(921, "	serious fire	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(922, "	animals on roadway	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(923, "	animals on the road. Danger	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(924, "	clearance work	",TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(925, "	blocked by storm damage	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(926, "	blocked by (Q) fallen trees	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(927, "	(Q) fallen tree(s). Passable with care	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(928, "	flooding. Stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(929, "	flooding. Danger of stationary traffic	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(930, "	flooding. Queuing traffic	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(931, "	flooding. Danger of queuing traffic	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(932, "	flooding. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(933, "	flooding. Slow traffic expected	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(934, "	flooding. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(935, "	flooding. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(936, "	flooding. Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(937, "	flooding. Traffic building up	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(938, "	closed due to flooding	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(939, "	flooding. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(940, "	flooding. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(941, "	flooding. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(942, "	flooding. Passable with care	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(943, "	closed due to avalanches	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(944, "	avalanches. Passable with care (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(945, "	closed due to rockfalls	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(946, "	rockfalls. Passable with care	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(947, "	road closed due to landslips	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(948, "	landslips. Passable with care	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(949, "	closed due to subsidence	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(950, "	subsidence. Single alternate line traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(951, "	subsidence. Carriageway reduced (from Q lanes) to one lane	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(952, "	subsidence. Carriageway reduced (from Q lanes) to two lanes	", TrafficEventType.AVOID, 0.4f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(953, "	subsidence. Carriageway reduced (from Q lanes) to three lanes	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(954, "	subsidence. Contraflow in operation	", TrafficEventType.AVOID, 0.8f, isReverse, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(955, "	subsidence. Passable with care	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(956, "	closed due to sewer collapse	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(957, "	road closed due to burst water main	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(958, "	burst water main. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(959, "	burst water main. Delays (Q) expected	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(960, "	burst water main. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(961, "	closed due to gas leak	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(962, "	gas leak. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(963, "	gas leak. Delays (Q) expected	", TrafficEventType.AVOID, 1.0f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(964, "	gas leak. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(965, "	closed due to serious fire	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(966, "	serious fire. Delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(967, "	serious fire. Delays (Q) expected	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(968, "	serious fire. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(969, "	closed for clearance work	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(970, "	road free again	", 0 , 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed
			new TrafficEventInfo(971, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(972, "	storm damage expected	",  TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(973, "	fallen power cables	",  TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(974, "	sewer overflow	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(975, "	ice build-up	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(976, "	mud slide	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(977, "	grass fire	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(978, "	air crash	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(979, "	rail crash	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(980, "	blocked by (Q) obstruction(s) on the road	",TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(981, "	(Q) obstructions on the road. Passable with care	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(982, "	blocked due to spillage on roadway	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(983, "	spillage on the road. Passable with care	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(984, "	spillage on the road. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(985, "	storm damage. Passable with care	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(986, "	storm damage. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(987, "	blocked by fallen power cables	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(988, "	fallen power cables. Passable with care	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(989, "	fallen power cables. Danger	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(990, "	sewer overflow. Danger	",  TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(991, "	flash floods. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(992, "	avalanches. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(993, "	closed due to avalanche risk	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(994, "	avalanche risk. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(995, "	closed due to ice build-up	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang,
			new TrafficEventInfo(996, "	ice build-up. Passable with care (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(997, "	ice build-up. Single alternate traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(998, "	rockfalls. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(999, "	landslips. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(1000, "	earthquake damage. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(1001, "	hazardous driving conditions (above Q hundred metres)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1002, "	danger of aquaplaning	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1003, "	slippery road (above Q hundred metres)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1004, "	mud on road	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1005, "	leaves on road	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1006, "	ice (above Q hundred metres)	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1007, "	danger of ice (above Q hundred metres)	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1008, "	black ice (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1009, "	freezing rain (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1010, "	wet and icy roads (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1011, "	slush (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1012, "	snow on the road (above Q hundred metres)	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1013, "	packed snow (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1014, "	fresh snow (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1015, "	deep snow (above Q hundred metres)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1016, "	snow drifts (above Q hundred metres)	",TrafficEventType.AVOID, 0.64f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1017, "	slippery due to spillage on roadway	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1018, "	slippery road (above Q hundred metres) due to snow	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1019, "	slippery road (above Q hundred metres) due to frost	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1020, "	road blocked by snow (above Q hundred metres)	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(1021, "	snow on the road. Carriageway reduced (from Q lanes) to one lane	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(1022, "	snow on the road. Carriageway reduced (from Q lanes) to two lanes	", TrafficEventType.AVOID, 0.4f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(1023, "	snow on the road. Carriageway reduced (from Q lanes) to three lanes	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(1024, "	conditions of road surface improved	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1025, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1026, "	subsidence. Danger	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1027, "	sewer collapse. Delays (Q)	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1028, "	sewer collapse. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1029, "	sewer collapse. Long delays (Q)	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1030, "	sewer collapse. Danger	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1031, "	burst water main. Danger	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1032, "	gas leak. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1033, "	serious fire. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1034, "	clearance work. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1035, "	impassable (above Q hundred metres)	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(1036, "	almost impassable (above Q hundred metres)	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.PARTIALLY_CLOSED), // changed by zwang
			new TrafficEventInfo(1037, "	extremely hazardous driving conditions (above Q hundred metres)	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.ROADWORKS), //changed by zwang
			new TrafficEventInfo(1038, "	difficult driving conditions (above Q hundred metres)	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.ROADWORKS), //changed by zwang,
			new TrafficEventInfo(1039, "	passable with care (up to Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1040, "	passable (up to Q hundred metres)	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1041, "	surface water hazard	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1042, "	loose sand on road	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1043, "	loose chippings	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1044, "	oil on road	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1045, "	petrol on road	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1046, "	ice expected (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1047, "	icy patches (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1048, "	danger of icy patches (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1049, "	icy patches expected (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1050, "	danger of black ice (above Q hundred metres)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang,
			new TrafficEventInfo(1051, "	black ice expected (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1052, "	freezing rain expected (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1053, "	snow drifts expected (above Q hundred metres)	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1054, "	slippery due to loose sand on roadway	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1055, "	mud on road. Danger	", 0, 1f, TrafficEventCategory.UNDEFINED), // changed
			new TrafficEventInfo(1056, "	loose chippings. Danger	", 0, 1f, TrafficEventCategory.UNDEFINED), // changed
			new TrafficEventInfo(1057, "	oil on road. Danger	", 0, 1f, TrafficEventCategory.UNDEFINED), // changed
			new TrafficEventInfo(1058, "	petrol on road. Danger	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1059, "	road surface in poor condition. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1060, "	icy patches (above Q hundred metres) on bridges	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1061, "	danger of icy patches (above Q hundred metres) on bridges	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1062,
					"	icy patches (above Q hundred metres) on bridges, in shaded areas and on slip roads	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1063, "	impassable for heavy vehicles (over Q)	", TrafficEventType.BLOCKED, 0.0f, TmcMode.HEAVY_VEHICLE, TrafficEventCategory.COMPLETELY_CLOSED), //changed by zwang
			new TrafficEventInfo(1064, "	impassable (above Q hundred metres) for vehicles with trailers	",  TrafficEventType.BLOCKED, 0.0f, TmcMode.VEHICLE_WITH_TRAILER, TrafficEventCategory.COMPLETELY_CLOSED), //changed by zwang
			new TrafficEventInfo(1065, "	driving conditions improved	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1066, "	rescue and recovery work in progress. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1067, "	large animals on roadway	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1068, "	herds of animals on roadway	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1069, "	skid hazard reduced	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1070, "	snow cleared	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1071, "	road conditions forecast withdrawn	",TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1072, "	message cancelled	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1073, "	extremely hazardous driving conditions expected (above Q hundred meters)	", TrafficEventType.AVOID, 0.2f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1074, "	freezing rain expected (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1075, "	danger of road being blocked by snow (above Q hundred metres)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1079, "	temperature falling rapidly (to Q)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1080, "	extreme heat (up to Q)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1081, "	extreme cold (of Q)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1082, "	less extreme temperatures	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1083, "	current temperature (Q)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1084, "	house fire	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1085, "	forest fire	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1086, "	vehicle stuck under bridge	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1090, "	volcano eruption warning	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1101, "	heavy snowfall (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1102, "	heavy snowfall (Q). Visibility reduced to <30 m	", TrafficEventType.AVOID, 0.4f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1103, "	heavy snowfall (Q). Visibility reduced to <50 m	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1104, "	snowfall (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1105, "	snowfall (Q). Visibility reduced to <100 m	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1106, "	hail (visibility reduced to Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1107, "	sleet (visibility reduced to Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1108, "	thunderstorms (visibility reduced to Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1109, "	heavy rain (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1110, "	heavy rain (Q). Visibility reduced to <30 m	", TrafficEventType.AVOID, 0.4f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1111, "	heavy rain (Q). Visibility reduced to <50 m	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1112, "	rain (Q)	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1113, "	rain (Q). Visibility reduced to <100 m	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1114, "	showers (visibility reduced to Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1115, "	heavy frost	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1116, "	frost	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1117, "	(Q probability of) overcast weather	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1118, "	(Q probability of) mostly cloudy	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1119, "	(Q probability of) partly cloudy	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1120, "	(Q probability of) sunny periods	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1121, "	(Q probability of) clear weather	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1122, "	(Q probability of) sunny weather	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1123, "	(Q probability of) mostly dry weather	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1124, "	(Q probability of) dry weather	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1125, "	sunny periods and with (Q probability of) showers	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1126, "	weather situation improved	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1127, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1128, "	winter storm (visibility reduced to Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1129, "	(Q probability of) winter storm	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1130, "	blizzard (visibility reduced to Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1131, "	(Q probability of) blizzard	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1132, "	damaging hail (visibility reduced to Q)	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1133, "	(Q probability of) damaging hail	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1134, "	heavy snowfall. Visibility reduced (to Q)	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1135, "	snowfall. Visibility reduced (to Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1136, "	heavy rain. Visibility reduced (to Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1137, "	rain. Visibility reduced (to Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1138, "	severe weather warnings cancelled	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1139, "	message cancelled	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1140, "	weather forecast withdrawn	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1141, "	fog forecast withdrawn	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1143, "	slippery road expected (above Q hundred metres)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1151, "	(Q probability of) heavy snowfall	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1152, "	(Q probability of) snowfall	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1153, "	(Q probability of) hail	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1154, "	(Q probability of) sleet	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1155, "	(Q probability of) thunderstorms	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1156, "	(Q probability of) heavy rain	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1157, "	(Q probability of) rain	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1158, "	(Q probability of) showers	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1159, "	(Q probability of) heavy frost	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1160, "	(Q probability of) frost	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1165, "	rain changing to snow	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1166, "	snow changing to rain	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1170, "	heavy snowfall (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1171, "	heavy rain (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1172, "	weather expected to improve	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1173, "	blizzard (with visibility reduced to Q) expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1174, "	damaging hail (with visibility reduced to Q) expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1175, "	reduced visibility (to Q) expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1176, "	freezing fog expected (with visibility reduced to Q). Danger of slippery roads	",
					TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1177, "	dense fog (with visibility reduced to Q) expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1178, "	patchy fog (with visibility reduced to Q) expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1179, "	visibility expected to improve	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1180, "	adverse weather warning withdrawn	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1190, "	severe smog	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1191, "	severe exhaust pollution	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1201, "	tornadoes	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1202, "	hurricane force winds (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1203, "	gales (Q)	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1204, "	storm force winds (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1205, "	strong winds (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1206, "	moderate winds (Q)	",TrafficEventType.AVOID, 0.9f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1207, "	light winds (Q)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1208, "	calm weather	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1209, "	gusty winds (Q)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1210, "	crosswinds (Q)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1211, "	strong winds (Q) affecting high-sided vehicles	", TrafficEventType.AVOID, 0.8f, TmcMode.HIGH_SIDED_VEHICLE, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1212, "	closed for high-sided vehicles due to strong winds (Q)	", TrafficEventType.BLOCKED, 0.0f, TmcMode.HIGH_SIDED_VEHICLE, TrafficEventCategory.COMPLETELY_CLOSED), //changed by zwang
			new TrafficEventInfo(1213, "	strong winds easing	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1214, "	message cancelled	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1215, "	restrictions for high-sided vehicles lifted	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1216, "	tornado watch cancelled	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1217, "	tornado warning ended	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1218, "	wind forecast withdrawn	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1219, "	message cancelled	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1251, "	(Q probability of) tornadoes	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1252, "	hurricane force winds (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1253, "	gales (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1254, "	storm force winds (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1255, "	strong winds (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1256, "	strong wind forecast withdrawn	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1300, "	snowfall and fog (visibility reduced to Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1301, "	dense fog (visibility reduced to Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1302, "	dense fog. Visibility reduced to <30 m	", TrafficEventType.AVOID, 0.4f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1303, "	dense fog. Visibility reduced to <50 m	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1304, "	fog (visibility reduced to Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1305, "	fog. Visibility reduced to <100 m	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1306, "	(Q probability of) fog	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1307, "	patchy fog (visibility reduced to Q)	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1308, "	freezing fog (visibility reduced to Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1309, "	smoke hazard (visibility reduced to Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1310, "	blowing dust (visibility reduced to Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1311, "	(Q probability of) severe exhaust pollution	",TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1312, "	snowfall and fog (visibility reduced to Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1313, "	visibility improved	",TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1314, "	message cancelled	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1315, "	(Q probability of) dense fog	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1316, "	(Q probability of) patchy fog	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1317, "	(Q probability of) freezing fog	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1318, "	visibility reduced (to Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1319, "	visibility reduced to <30 m	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1320, "	visibility reduced to <50 m	", TrafficEventType.AVOID, 4f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1321, "	visibility reduced to <100 m	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1322, "	white out (visibility reduced to Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1323, "	blowing snow (visibility reduced to Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1324, "	spray hazard (visibility reduced to Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1325, "	low sun glare	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1326, "	sandstorms (visibility reduced to Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1327, "	(Q probability of) sandstorms	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1328, "	(Q probability of) air quality: good	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1329, "	(Q probability of) air quality: fair	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1330, "	(Q probability of) air quality: poor	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1331, "	(Q probability of) air quality: very poor	", TrafficEventType.AVOID, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1332, "	smog alert	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1333, "	(Q probability of) smog	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1334, "	(Q probability of) pollen count: high	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1335, "	(Q probability of) pollen count: medium	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1336, "	(Q probability of) pollen count: low	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang,
			new TrafficEventInfo(1337, "	freezing fog (visibility reduced to Q). Slippery roads	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1338, "	no motor vehicles due to smog alert	",  TrafficEventType.BLOCKED, 0f, TrafficEventCategory.COMPLETELY_CLOSED), //changed by zwang
			new TrafficEventInfo(1339, "	air quality improved	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1340, "	swarms of insects (visibility reduced to Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), //changed by zwang
			new TrafficEventInfo(1345, "	fog clearing	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1346, "	fog forecast withdrawn	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1351, "	maximum temperature (of Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1352, "	hot, (maximum temperature Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1353, "	warm, (maximum temperature Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1354, "	mild, (maximum temperature Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1355, "	cool, (maximum temperature Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1356, "	cold, (maximum temperature Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1357, "	very cold, (maximum temperature Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1358, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1359, "	temperature rising (to Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1360, "	temperature falling rapidly (to Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1361, "	temperature (Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1362, "	effective temperature, with wind chill (Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1364, "	extreme heat (up to Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1365, "	extreme cold (of Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang,
			new TrafficEventInfo(1401, "	minimum temperature (of Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1402, "	very warm (minimum temperature Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1403, "	warm (minimum temperature Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1404, "	mild (minimum temperature Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1405, "	cool (minimum temperature Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1406, "	cold (minimum temperature Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1407, "	very cold (minimum temperature Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1408, "	less extreme temperatures expected	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1449, "	emergency training in progress	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1450, "	international sports meeting	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1451, "	match	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1452, "	tournament	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1453, "	athletics meeting	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1454, "	ball game	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1455, "	boxing tournament	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1456, "	bull fight	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1457, "	cricket match	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1458, "	cycle race	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1459, "	football match	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1460, "	golf tournament	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1461, "	marathon	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1462, "	race meeting	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1463, "	rugby match	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1464, "	show jumping	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1465, "	tennis tournament	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1466, "	water sports meeting	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1467, "	winter sports meeting	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1468, "	funfair	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1469, "	trade fair	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1470, "	procession	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), //changed by zwang
			new TrafficEventInfo(1471, "	sightseers obstructing access	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1472, "	people on roadway	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1473, "	children on roadway	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1474, "	cyclists on roadway	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang,
			new TrafficEventInfo(1475, "	strike	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1476, "	security incident	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1477, "	police checkpoint	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1478, "	terrorist incident	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1479, "	gunfire on roadway, danger	", TrafficEventType.BLOCKED, 0f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1480, "	civil emergency	", TrafficEventType.BLOCKED, 0f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1481, "	air raid, danger	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1482, "	people on roadway. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1483, "	children on roadway. Danger	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1484, "	cyclists on roadway. Danger	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1485, "	closed due to security incident	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1486, "	security incident. Delays (Q)	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1487, "	security incident. Delays (Q) expected	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1488, "	security incident. Long delays (Q)	",  TrafficEventType.AVOID, 0.5f, TrafficEventCategory.WARNING), //changed by zwang
			new TrafficEventInfo(1489, "	police checkpoint. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1490, "	police checkpoint. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1491, "	police checkpoint. Long delays (Q)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1492, "	security alert withdrawn	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1493, "	sports traffic cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1494, "	evacuation	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1495, "	evacuation. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1496, "	traffic disruption cleared	",	TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1497, "	military training in progress	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1498, "	police activity ongoing	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1499, "	medical emergency ongoing	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1500, "	child abduction in progress	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1501, "	major event	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1502, "	sports event meeting	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1503, "	show	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1504, "	festival	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1505, "	exhibition	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1506, "	fair	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1507, "	market	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1508, "	ceremonial event	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1509, "	state occasion	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1510, "	parade	",   TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1511, "	crowd	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1512, "	march	",   TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1513, "	demonstration	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1514, "	public disturbance	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1515, "	security alert	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1516, "	bomb alert	",TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1517, "	major event. Stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1518, "	major event. Danger of stationary traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1519, "	major event. Queuing traffic	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1520, "	major event. Danger of queuing traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1521, "	major event. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1522, "	major event. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1523, "	major event. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1524, "	major event. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1525, "	major event. Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1526, "	major event. Traffic building up	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1527, "	closed due to major event	",TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(1528, "	major event. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1529, "	major event. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1530, "	major event. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1531, "	sports meeting. Stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1532, "	sports meeting. Danger of stationary traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1533, "	sports meeting. Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1534, "	sports meeting. Danger of queuing traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1535, "	sports meeting. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1536, "	sports meeting. Slow traffic expected	",  TrafficEventType.AVOID, 1f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1537, "	sports meeting. Heavy traffic	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1538, "	sports meeting. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1539, "	sports meeting. Traffic flowing freely	",  TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1540, "	sports meeting. Traffic building up	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1541, "	closed due to sports meeting	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(1542, "	sports meeting. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1543, "	sports meeting. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1544, "	sports meeting. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1545, "	fair. Stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1546, "	fair. Danger of stationary traffic	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1547, "	fair. Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1548, "	fair. Danger of queuing traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1549, "	fair. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1550, "	fair. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1551, "	fair. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1552, "	fair. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1553, "	fair. Traffic flowing freely	",TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang,
			new TrafficEventInfo(1554, "	fair. Traffic building up	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang,
			new TrafficEventInfo(1555, "	closed due to fair	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(1556, "	fair. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1557, "	fair. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1558, "	fair. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1559, "	closed due to parade	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(1560, "	parade. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1561, "	parade. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1562, "	parade. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1563, "	closed due to strike	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(1564, "	strike. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1565, "	strike. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1566, "	strike. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1567, "	closed due to demonstration	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(1568, "	demonstration. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1569, "	demonstration. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1570, "	demonstration. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1571, "	security alert. Stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1572, "	security alert. Danger of stationary traffic	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1573, "	security alert. Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1574, "	security alert. Danger of queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1575, "	security alert. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1576, "	security alert. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1577, "	security alert. Heavy traffic	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1578, "	security alert. Heavy traffic expected	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1579, "	security alert. Traffic building up	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1580, "	closed due to security alert	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(1581, "	security alert. Delays (Q)	", 	TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1582, "	security alert. Delays (Q) expected	",	TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1583, "	security alert. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1584, "	traffic has returned to normal	", 0, 1f, TrafficEventCategory.NORMAL_TRAFFIC),   // Changed
			new TrafficEventInfo(1585, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1586, "	security alert. Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1587, "	air raid warning cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1588, "	civil emergency cancelled	",TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1589, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1590, "	several major events	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1591, "	information about major event no longer valid	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1592, "	automobile race	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1593, "	baseball game	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1594, "	basketball game	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1595, "	boat race	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1596, "	concert	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1597, "	hockey game	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1601, "	delays (Q)	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1602, "	delays up to 15 minutes	",  TrafficEventType.AVOID, true, 15f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1603, "	delays up to 30 minutes	",  TrafficEventType.AVOID, true, 30f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1604, "	delays up to one hour	",  TrafficEventType.AVOID, true, 60f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1605, "	delays up to two hours	",  TrafficEventType.AVOID, true, 2*60f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1606, "	delays of several hours	",  TrafficEventType.AVOID, true, 3*60f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1607, "	delays (Q) expected	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1608, "	long delays (Q)	", TrafficEventType.ANY, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1609, "	delays (Q) for heavy vehicles	", TrafficEventType.AVOID, true, 15f, TmcMode.HEAVY_VEHICLE, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1610, "	delays up to 15 minutes for heavy lorr(y/ies)	", TrafficEventType.AVOID, true, 15f, TmcMode.HEAVY_VEHICLE, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1611, "	delays up to 30 minutes for heavy lorr(y/ies)	", TrafficEventType.AVOID, true, 30f, TmcMode.HEAVY_VEHICLE, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1612, "	delays up to one hour for heavy lorr(y/ies)	", TrafficEventType.AVOID, true, 60f, TmcMode.HEAVY_VEHICLE, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1613, "	delays up to two hours for heavy lorr(y/ies)	", TrafficEventType.AVOID, true, 2*60f, TmcMode.HEAVY_VEHICLE, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1614, "	delays of several hours for heavy lorr(y/ies)	", TrafficEventType.AVOID, true, 3*60f, TmcMode.HEAVY_VEHICLE, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1615, "	service suspended (until Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1616, "	(Q) service withdrawn	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1617, "	(Q) service(s) fully booked	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1618, "	(Q) service(s) fully booked for heavy vehicles	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1619, "	normal services resumed	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1620, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1621, "	delays up to 5 minutes	",  TrafficEventType.AVOID, true, 5f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1622, "	delays up to 10 minutes	",  TrafficEventType.AVOID, true, 10f, TrafficEventCategory.WARNING), // changed by zwang,
			new TrafficEventInfo(1623, "	delays up to 20 minutes	",  TrafficEventType.AVOID, true, 20f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1624, "	delays up to 25 minutes	",  TrafficEventType.AVOID, true, 25f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1625, "	delays up to 40 minutes	",  TrafficEventType.AVOID, true, 40f, TrafficEventCategory.WARNING), // changed by zwang,
			new TrafficEventInfo(1626, "	delays up to 50 minutes	",  TrafficEventType.AVOID, true, 50f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1627, "	delays up to 90 minutes	",  TrafficEventType.AVOID, true, 90f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1628, "	delays up to three hours	", TrafficEventType.AVOID, true, 3*60f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1629, "	delays up to four hours	",  TrafficEventType.AVOID, true, 4*60f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1630, "	delays up to five hours	",  TrafficEventType.AVOID, true, 5*60f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1631, "	very long delays (Q)	",  TrafficEventType.AVOID, true, 6*60f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1632, "	delays of uncertain duration	",  TrafficEventType.AVOID, true, 60f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1633, "	delayed until further notice	", TrafficEventType.AVOID, true, 60f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1634, "	cancellations	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1635, "	park and ride service not operating (until Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1636, "	special public transport services operating (until Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1637, "	normal services not operating (until Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1638, "	rail services not operating (until Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1639, "	bus services not operating (until Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1640, "	shuttle service operating (until Q)	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1641, "	free shuttle service operating (until Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1642, "	delays (Q) for heavy lorr(y/ies)	", TrafficEventType.AVOID, true, 15f, TmcMode.LORRIES, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1643, "	delays (Q) for buses	", TrafficEventType.AVOID, true, 15f, TmcMode.BUS, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1644, "	(Q) service(s) fully booked for heavy lorr(y/ies)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1645, "	(Q) service(s) fully booked for buses	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1646, "	next departure (Q) for heavy lorr(y/ies)	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1647, "	next departure (Q) for buses	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1648, "	delays cleared	",  TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1649, "	rapid transit service not operating (until Q)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1650, "	delays (Q) possible	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1651, "	underground service not operating (until Q)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1652, "	cancellations expected	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1653, "	long delays expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1654, "	very long delays expected	",TrafficEventType.AVOID, 0.3f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1655, "	all services fully booked (until Q)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1656, "	next arrival (Q)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang
			new TrafficEventInfo(1657, "	rail services irregular. Delays (Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1658, "	bus services irregular. Delays (Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1659, "	underground services irregular	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1660, "	normal public transport services resumed	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1661, "	ferry service not operating (until Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1662, "	park and ride trip time (Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang
			new TrafficEventInfo(1663, "	delay expected to be cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang
			new TrafficEventInfo(1664, "	demonstration by vehicles	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang
			new TrafficEventInfo(1680, "	delays (Q) have to be expected	", TrafficEventType.ANY, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1681, "	delays of several hours have to be expected	", TrafficEventType.ANY, true, 6*60f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1682, "	closed ahead. Delays (Q) have to be expected	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(1683, "	roadworks. Delays (Q) have to be expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.ROADWORKS), //canged by zwang
			new TrafficEventInfo(1684, "	flooding. Delays (Q) have to be expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1685, "	major event. Delays (Q) have to be expected	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1686, "	strike. Delays (Q) have to be expected	",  TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1687, "	delays of several hours for heavy lorries have to be expected	",  TrafficEventType.AVOID, true, 6*60f, TmcMode.HEAVY_VEHICLE, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1688, "	long delays have to be expected	",  TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1689, "	very long delays have to be expected	",  TrafficEventType.AVOID, 0.4f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1690, "	delay forecast withdrawn	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang
			new TrafficEventInfo(1691, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1695, "	current trip time (Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang
			new TrafficEventInfo(1696, "	expected trip time (Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang
			new TrafficEventInfo(1700, "	(Q) slow moving maintenance vehicle(s)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1701, "	(Q) vehicle(s) on wrong carriageway	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1702, "	dangerous vehicle warning cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1703, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1704, "	(Q) reckless driver(s)	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1705, "	(Q) prohibited vehicle(s) on the roadway	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1706, "	(Q) emergency vehicles	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1707, "	(Q) high-speed emergency vehicles	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1708, "	high-speed chase (involving Q vehicles)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1709, "	spillage occurring from moving vehicle	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1710, "	objects falling from moving vehicle	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang,
			new TrafficEventInfo(1711, "	emergency vehicle warning cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1712, "	road cleared	",TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1720, "	rail services irregular	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1721, "	public transport services not operating	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1731, "	(Q) abnormal load(s), danger	",  TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1732, "	(Q) wide load(s), danger	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1733, "	(Q) long load(s), danger	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1734, "	(Q) slow vehicle(s), danger	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1735, "	(Q) track-laying vehicle(s), danger	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1736, "	(Q) vehicle(s) carrying hazardous materials. Danger	",TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1737, "	(Q) convoy(s), danger	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1738, "	(Q) military convoy(s), danger	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1739, "	(Q) overheight load(s), danger	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1740, "	abnormal load causing slow traffic. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1741, "	convoy causing slow traffic. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1751, "	(Q) abnormal load(s)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1752, "	(Q) wide load(s)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1753, "	(Q) long load(s)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1754, "	(Q) slow vehicle(s)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1755, "	(Q) convoy(s)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1756, "	abnormal load. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1757, "	abnormal load. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1758, "	abnormal load. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang,
			new TrafficEventInfo(1759, "	convoy causing delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1760, "	convoy. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1761, "	convoy causing long delays (Q)	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1762, "	exceptional load warning cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1763, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1764, "	(Q) track-laying vehicle(s)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1765, "	(Q) vehicle(s) carrying hazardous materials	",  TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1766, "	(Q) military convoy(s)	", TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1767, "	(Q) abnormal load(s). No overtaking	",  TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1768, "	Vehicles carrying hazardous materials have to stop at next safe place!	",  TrafficEventType.AVOID, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1769, "	hazardous load warning cleared	",  TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1770, "	convoy cleared	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1771, "	warning cleared	",TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1780, "	cancellations have to be expected	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1781, "	all services fully booked (until Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1782, "	park and ride service will not be operating (until Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1783, "	normal services will not be operating (until Q)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1784, "	rail services will not be operating (until Q)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1785, "	rapid transit service will not be operating (until Q)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1786, "	underground service will not be operating (until Q)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1787, "	public transport will be on strike	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1788, "	ferry service will not be operating (until Q)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1789, "	normal services expected	",  TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang
			new TrafficEventInfo(1790, "	message cancelled	",  TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang
			new TrafficEventInfo(1801, "	lane control signs not working	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1802, "	emergency telephones not working	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1803, "	emergency telephone number not working	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1804, "	(Q sets of) traffic lights not working	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1805, "	(Q sets of) traffic lights working incorrectly	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1806, "	level crossing failure	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1807, "	(Q sets of) traffic lights not working. Stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1808, "	(Q sets of) traffic lights not working. Danger of stationary traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1809, "	(Q sets of) traffic lights not working. Queuing traffic	",TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1810, "	(Q sets of) traffic lights not working. Danger of queuing traffic	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1811, "	(Q sets of) traffic lights not working. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1812, "	(Q sets of) traffic lights not working. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1813, "	(Q sets of) traffic lights not working. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1814, "	(Q sets of) traffic lights not working. Heavy traffic expected	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1815, "	(Q sets of) traffic lights not working. Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1816, "	(Q sets of) traffic lights not working. Traffic building up	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1817, "	traffic lights not working. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1818, "	traffic lights not working. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1819, "	traffic lights not working. Long delays (Q)	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1820, "	level crossing failure. Stationary traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1821, "	level crossing failure. Danger of stationary traffic	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1822, "	level crossing failure. Queuing traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1823, "	level crossing failure. Danger of queuing traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.STATIONARY_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1824, "	level crossing failure. Slow traffic	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1825, "	level crossing failure. Slow traffic expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1826, "	level crossing failure. Heavy traffic	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1827, "	level crossing failure. Heavy traffic expected	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1828, "	level crossing failure. Traffic flowing freely	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1829, "	level crossing failure. Traffic building up	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1830, "	level crossing failure. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1831, "	level crossing failure. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1832, "	level crossing failure. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1833, "	electronic signs repaired	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1834, "	emergency call facilities restored	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang
			new TrafficEventInfo(1835, "	traffic signals repaired	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang
			new TrafficEventInfo(1836, "	level crossing now working normally	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1837, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.NORMAL_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1838, "	lane control signs working incorrectly	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1839, "	lane control signs operating	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1840, "	variable message signs not working	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1841, "	variable message signs working incorrectly	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1842, "	variable message signs operating	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1843, "	(Q sets of) ramp control signals not working	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1844, "	(Q sets of) ramp control signals working incorrectly	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1845, "	(Q sets of) temporary traffic lights not working	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1846, "	(Q sets of) temporary traffic lights working incorrectly	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1847, "	traffic signal control computer not working	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1848, "	traffic signal timings changed	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1849, "	tunnel ventilation not working	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1850, "	lane control signs not working. Danger	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1851, "	temporary width limit (Q)	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1852, "	temporary width limit lifted	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1854, "	traffic regulations have been changed	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1855, "	less than 50 parking spaces available	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1856, "	no parking information available (until Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1857, "	message cancelled	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1858, "	Snowplough. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1861, "	temporary height limit (Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1862, "	temporary height limit lifted	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1863, "	(Q) automatic payment lanes not working	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1864, "	lane control signs working incorrectly. Danger	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1865, "	emergency telephones out of order. Extra police patrols in operation	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1866, "	emergency telephones out of order. In emergency, wait for police patrol	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1867, "	(Q sets of) traffic lights not working. Danger	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1868, "	traffic lights working incorrectly. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1869, "	traffic lights working incorrectly. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1870, "	traffic lights working incorrectly. Long delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1871, "	temporary axle load limit (Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed,
			new TrafficEventInfo(1872, "	temporary gross weight limit (Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1873, "	temporary gross weight limit lifted	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1874, "	temporary axle weight limit lifted	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1875, "	(Q sets of) traffic lights working incorrectly. Danger	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1876, "	temporary traffic lights not working. Delays (Q)	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1877, "	temporary traffic lights not working. Delays (Q) expected	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1878, "	temporary traffic lights not working. Long delays (Q)	",TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1879, "	(Q sets of) temporary traffic lights not working. Danger	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1880, "	traffic signal control computer not working. Delays (Q)	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1881, "	temporary length limit (Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1882, "	temporary length limit lifted	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1883, "	message cancelled	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1884, "	traffic signal control computer not working. Delays (Q) expected	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1885, "	traffic signal control computer not working. Long delays (Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1886, "	normal parking restrictions lifted	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1887, "	special parking restrictions in force	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1888, "	10% full	", TrafficEventType.AVOID, 0.9f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1889, "	20% full	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1890, "	30% full	", TrafficEventType.AVOID, 0.7f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1891, "	40% full	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1892, "	50% full	", TrafficEventType.AVOID, 0.5f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang,
			new TrafficEventInfo(1893, "	60% full	", TrafficEventType.AVOID, 0.4f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1894, "	70% full	", TrafficEventType.AVOID, 0.3f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1895, "	80% full	", TrafficEventType.AVOID, 0.2f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1896, "	90% full	", TrafficEventType.AVOID, 0.1f, TrafficEventCategory.SLOW_TRAFFIC), // changed by zwang
			new TrafficEventInfo(1897, "	less than 10 parking spaces available	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1898, "	less than 20 parking spaces available	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1899, "	less than 30 parking spaces available	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1900, "	less than 40 parking spaces available	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1901, "	next departure (Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1902, "	next departure (Q) for heavy vehicles	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1903, "	car park (Q) full	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1904, "	all car parks (Q) full	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1905, "	less than (Q) car parking spaces available	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1906, "	park and ride service operating (until Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1907, "	(null event) {no event description, but location etc. given in message}	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1908, "	switch your car radio (to Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1909,
					"	alarm call: important new information on this frequency follows now in normal programme	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1910,
					"	alarm set: new information will be broadcast between these times in normal programme	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1911, "	message cancelled	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1913, "	switch your car radio (to Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1914, "	no information available (until Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1915, "	this message is for test purposes only (number Q), please ignore	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1916, "	no information available (until Q) due to technical problems	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1917, "	automatic toll system not working, pay manually	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1918, "	full	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1920, "	only a few parking spaces available	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1921, "	(Q) parking spaces available	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1922, "	expect car park to be full	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1923, "	expect no parking spaces available	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1924, "	multi story car parks full	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1925, "	no problems to report with park and ride services	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1926, "	no parking spaces available	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1927, "	no parking (until Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1928, "	special parking restrictions lifted	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1929, "	urgent information will be given (at Q) on normal programme broadcasts	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1930, "	this TMC-service is not active (until Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1931, "	detailed information will be given (at Q) on normal programme broadcasts	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1932, "	detailed information is provided by another TMC service	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1934, "	no park and ride information available (until Q)	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1938, "	park and ride information service resumed	", 0, 1f, TrafficEventCategory.WARNING), // changed
			new TrafficEventInfo(1939, "	travel information telephone service availiable	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1940, "	additional regional information is provided by another TMC service	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1941, "	additional local information is provided by another TMC service	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1942, "	additional public transport information is provided by another TMC service	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1943, "	national traffic information is provided by another TMC service	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1944, "	this service provides major road information	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1945, "	this service provides regional travel information	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1946, "	this service provides local travel information	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1947, "	no detailed regional information provided by this service	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1948, "	no detailed local information provided by this service	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1949, "	no cross-border information provided by this service	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1950, "	information restricted to this area	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1951, "	no new traffic information available (until Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1952, "	no public transport information available	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1953, "	this TMC-service is being suspended (at Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1954, "	active TMC-service will resume (at Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1955, "	reference to audio programmes no longer valid	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1956, "	reference to other TMC services no longer valid	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1957, "	previous announcement about this or other TMC services no longer valid	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1961, "	allow emergency vehicles to pass in the carpool lane	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1962, "	carpool lane available for all vehicles	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1963, "	police directing traffic via the carpool lane	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1964, "	rail information service not available	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1965, "	rail information service resumed	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1966, "	rapid transit information service not available	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1967, "	rapid transit information service resumed	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1971, "	police directing traffic	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1972, "	buslane available for all vehicles	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1973, "	police directing traffic via the buslane	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1974, "	allow emergency vehicles to pass	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1975, "	overtaking prohibited for heavy vehicles (over Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1976, "	overtaking prohibited	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1977, "	allow emergency vehicles to pass in the heavy vehicle lane	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1978, "	heavy vehicle lane available for all vehicles	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1979, "	police directing traffic via the heavy vehicle lane	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1980, "	overtaking prohibited for heavy lorries (over Q)	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1981, "	drivers of heavy lorries (over Q) are recommended to stop at next safe place	", 0,
					1f, TrafficEventCategory.UNDEFINED), // changed
			new TrafficEventInfo(1982, "	buslane closed	",TrafficEventType.BLOCKED, 0.0f, TmcMode.BUS, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1983, "	power failure	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1985, "	overtaking restriction lifted	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1986, "	Low Emission Zone restriction in force	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1990, "	car park closed (until Q)	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1991, "	danger of waiting vehicles on roadway	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1993, "	number of parking spaces decreasing	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1994, "	number of parking spaces constant	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1995, "	number of parking spaces increasing	",  TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1998, "	dangerous situation on exit slip road	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(1999, "	dangerous situation on entry slip road	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2000, "	closed due to smog alert (until Q)	", TrafficEventType.BLOCKED, 0.0f, TrafficEventCategory.COMPLETELY_CLOSED), // changed by zwang
			new TrafficEventInfo(2006, "	closed for vehicles with less than three occupants {not valid for lorries}	", 0,
					1f),
			new TrafficEventInfo(2007, "	closed for vehicles with only one occupant {not valid for lorries}	", 0, 1f),
			new TrafficEventInfo(2013, "	service area busy	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang 
			new TrafficEventInfo(2021, "	service not operating, substitute service available	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang 
			new TrafficEventInfo(2022, "	public transport strike	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang 
			new TrafficEventInfo(2028, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang 
			new TrafficEventInfo(2029, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2030, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2032, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2033, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2034, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2035, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2038, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2039, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2040, "	message cancelled	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2041, "	nothing to report	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2042, "	ice build-up on cable structure	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2043, "	road salted	", TrafficEventType.ANY, 1f, TrafficEventCategory.UNDEFINED), // changed by zwang 
			new TrafficEventInfo(2044, "	danger of snow patches	", TrafficEventType.AVOID, 0.8f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2045, "	snow patches	", TrafficEventType.AVOID, 0.6f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2046, "	Convoy service required due to bad weather	",TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING), // changed by zwang
			new TrafficEventInfo(2047, "	(null message) {completely silent message, see protocol, sect. 3.5.4}	", TrafficEventType.ANY, 1f, TrafficEventCategory.WARNING)  };// changed by zwang

	static
	{
		_dictCodes = new HashMap<Integer, TrafficEventInfo>();
		for (int j = 0; j < CODES.length; j++) 
		{
			TrafficEventInfo tec = CODES[j];
			_dictCodes.put(tec.code, tec);
		}
	}
	
	public static TrafficEventInfo getEventInfo(int code) {
		return _dictCodes.get(code);
	}
	
	public static void saveToFile(File file) throws IOException
	{
		HashMap <Integer, List<TrafficEventInfo>> dict = new HashMap<>();
		for (int j = 0; j < CODES.length; j++) {
			TrafficEventInfo tec = CODES[j];
			
			List<TrafficEventInfo> list;
			if (!dict.containsKey(tec.category))
			{
				list = new ArrayList<TrafficEventInfo>();
			    dict.put(tec.category, list);
			}
			else
			{
				list = dict.get(tec.category);
			}
			 
			list.add(tec);
		}

		FileWriter fw = new FileWriter(file);
		String newLine = System.getProperty("line.separator");
		for(Entry<Integer, List<TrafficEventInfo>> entry : dict.entrySet())
		{
			
			fw.write("#" + TrafficEventCategory.toString(entry.getKey()) + newLine);

			String codes = "";
			List<TrafficEventInfo> list = entry.getValue();
			int i = 0;
			int n = list.size();
			for(TrafficEventInfo tei : list)
			{
				codes += tei.code;

				if (i < n - 1) 
					codes += ",";
				
				i++;
			}
			
			fw.write(codes + newLine);
			fw.write(newLine);
		}
		
        fw.close();
	}
}
