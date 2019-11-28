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
package org.heigit.ors.routing.instructions;

import com.graphhopper.util.Helper;
import org.heigit.ors.common.ArrivalDirection;
import org.heigit.ors.common.CardinalDirection;
import org.heigit.ors.localization.LanguageResources;

public class InstructionTranslator {
	private static final String STR_TURN_MANEUVER = "{turn_maneuver}";
	private static final String STR_WAY_NAME = "{way_name}";
	private static final String STR_EXIT_NUMBER = "{exit_number}";
	private static final String STR_DIRECTION = "{direction}";

	private LanguageResources resources;
	private String[] directions;
	private String actionDepartDefault;
	private String actionDepartName;
	private String[] actionArriveDefault;
	private String[] actionArriveName;
	private String actionRoundaboutDefault;
	private String actionRoundaboutName;
	private String actionContinueDefault;
	private String actionContinueName;
	private String actionKeepDefault;
	private String actionKeepName;
	private String actionTurnDefault;
	private String actionTurnName;
	private String[] numerals;
	private String[] turnManeuvers;

	InstructionTranslator(LanguageResources resources) throws Exception {
		this.resources = resources;

		directions = new String[8];
		directions[0] = this.resources.getTranslation("instructions.directions.north");
		directions[1] = this.resources.getTranslation("instructions.directions.northeast");
		directions[2] = this.resources.getTranslation("instructions.directions.east");
		directions[3] = this.resources.getTranslation("instructions.directions.southeast");
		directions[4] = this.resources.getTranslation("instructions.directions.south");
		directions[5] = this.resources.getTranslation("instructions.directions.southwest");
		directions[6] = this.resources.getTranslation("instructions.directions.west");
		directions[7] = this.resources.getTranslation("instructions.directions.northwest");

		turnManeuvers = new String[10];
		turnManeuvers[0] = this.resources.getTranslation("instructions.turn_maneuvers.left");
		turnManeuvers[1] = this.resources.getTranslation("instructions.turn_maneuvers.right");
		turnManeuvers[2] = this.resources.getTranslation("instructions.turn_maneuvers.sharp_left");
		turnManeuvers[3] = this.resources.getTranslation("instructions.turn_maneuvers.sharp_right");
		turnManeuvers[4] = this.resources.getTranslation("instructions.turn_maneuvers.slight_left");
		turnManeuvers[5] = this.resources.getTranslation("instructions.turn_maneuvers.slight_right");
		turnManeuvers[6] = this.resources.getTranslation("instructions.turn_maneuvers.straight");
		turnManeuvers[7] = this.resources.getTranslation("instructions.turn_maneuvers.uturn");
		turnManeuvers[8] = this.resources.getTranslation("instructions.turn_maneuvers.left");
		turnManeuvers[9] = this.resources.getTranslation("instructions.turn_maneuvers.right");

		numerals = new String[11];
		for (int i = 1; i<=10; i++)
			numerals[i] = this.resources.getTranslation("instructions.numerals." + i);

		actionDepartDefault = this.resources.getTranslation("instructions.actions.depart.default.default");
		actionDepartName = this.resources.getTranslation("instructions.actions.depart.default.name");
		actionContinueDefault = this.resources.getTranslation("instructions.actions.continue.default.default");
		actionContinueName = this.resources.getTranslation("instructions.actions.continue.default.name");
		actionKeepDefault = this.resources.getTranslation("instructions.actions.keep.default.default");
		actionKeepName = this.resources.getTranslation("instructions.actions.keep.default.name");
		actionTurnDefault = this.resources.getTranslation("instructions.actions.turn.default.default");
		actionTurnName = this.resources.getTranslation("instructions.actions.turn.default.name");
		actionRoundaboutDefault = this.resources.getTranslation("instructions.actions.roundabout.default.exit.default");
		actionRoundaboutName = this.resources.getTranslation("instructions.actions.roundabout.default.exit.name");
		
		actionArriveDefault = new String[4];
		actionArriveName = new String[4];
		actionArriveDefault[0] = this.resources.getTranslation("instructions.actions.arrive.default.default");
		actionArriveDefault[1] = this.resources.getTranslation("instructions.actions.arrive.left.default");
		actionArriveDefault[2] = this.resources.getTranslation("instructions.actions.arrive.right.default");
		actionArriveDefault[3] = this.resources.getTranslation("instructions.actions.arrive.straight.default");
		actionArriveName[0] = this.resources.getTranslation("instructions.actions.arrive.default.name");
		actionArriveName[1] = this.resources.getTranslation("instructions.actions.arrive.left.name");
		actionArriveName[2] = this.resources.getTranslation("instructions.actions.arrive.right.name");
		actionArriveName[3] = this.resources.getTranslation("instructions.actions.arrive.straight.name");
	}

	public String getContinue(InstructionType type, String wayName) {
		if (Helper.isEmpty(wayName))
			return actionContinueDefault.replace(STR_TURN_MANEUVER,  turnManeuvers[getTurnManeuver(type)]);
		else 
			return actionContinueName.replace(STR_TURN_MANEUVER, turnManeuvers[getTurnManeuver(type)]).replace(STR_WAY_NAME, wayName);
	}

	public String getTurn(InstructionType type, String wayName) {
		if (Helper.isEmpty(wayName))
			return actionTurnDefault.replace(STR_TURN_MANEUVER,  turnManeuvers[getTurnManeuver(type)]);
		else 
			return actionTurnName.replace(STR_TURN_MANEUVER, turnManeuvers[getTurnManeuver(type)]).replace(STR_WAY_NAME, wayName);
	}

	public String getKeep(InstructionType type, String wayName) {
		if (Helper.isEmpty(wayName))
			return actionKeepDefault.replace(STR_TURN_MANEUVER,  turnManeuvers[getTurnManeuver(type)]);
		else
			return actionKeepName.replace(STR_TURN_MANEUVER, turnManeuvers[getTurnManeuver(type)]).replace(STR_WAY_NAME, wayName);
	}

	public String getRoundabout(int exitNumber, String wayName) {
		boolean isWayNull = Helper.isEmpty(wayName);
		String str = isWayNull ? actionRoundaboutDefault : actionRoundaboutName;
		boolean isExitNull = (exitNumber == 0);

		// We need to check if the exit number is greater than 10, as that is the most we have in the n-th representation
		boolean highNumber = (exitNumber > numerals.length-1);

		//If there was an error in finding the exit number, return "UNKNOWN". If there is no way name, don't return a way name
		if(isExitNull)
			str = str.replace(STR_EXIT_NUMBER,  "UNKNOWN");
		else {
			String numeral;
			if(highNumber) {
				// if it is a high number which is very rare, then we dont use the numeral representation, just the
				// number itself
				// Converting to the th is too complicated due to exceptions and the position of the "th"

				numeral = Integer.toString(exitNumber);
			} else {
				numeral = numerals[exitNumber];
			}

			str = str.replace(STR_EXIT_NUMBER, numeral);
		}
		if (isWayNull)
			return str;
		else 
			return str.replace(STR_WAY_NAME, wayName);
	}

	public String getDepart(CardinalDirection direction, String wayName) {
		if (Helper.isEmpty(wayName))
			return actionDepartDefault.replace(STR_DIRECTION, directions[direction.ordinal()]);
		else 
			return actionDepartName.replace(STR_DIRECTION, directions[direction.ordinal()]).replace(STR_WAY_NAME, wayName);
	}
	
	public String getArrive(ArrivalDirection direction, String wayName) {
		if (Helper.isEmpty(wayName))
			return actionArriveDefault[direction.ordinal()];
		else 
			return actionArriveName[direction.ordinal()].replace(STR_WAY_NAME, wayName);
	}

	private int getTurnManeuver(InstructionType type) {
	    switch (type){
			default:
            case TURN_LEFT:
                return 0;
            case TURN_RIGHT:
                return 1;
            case TURN_SHARP_LEFT:
                return 2;
            case TURN_SHARP_RIGHT:
                return 3;
            case TURN_SLIGHT_LEFT:
                return 4;
            case TURN_SLIGHT_RIGHT:
                return 5;
            case CONTINUE:
                return 6;
            case KEEP_LEFT:
                return 8;
            case KEEP_RIGHT:
                return 9;
        }
		//TODO
		//	_turnManeuvers[7] = _resources.getTranslation("instructions.turn_maneuvers.uturn")
	}
}
