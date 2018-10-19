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
package heigit.ors.routing.instructions;

import com.graphhopper.util.Helper;

import heigit.ors.common.ArrivalDirection;
import heigit.ors.common.CardinalDirection;
import heigit.ors.localization.LanguageResources;

public class InstructionTranslator 
{
	private LanguageResources _resources;

	private String[] _directions;
	private String _actionDepartDefault;
	private String _actionDepartName;
	private String[] _actionArriveDefault;	
	private String[] _actionArriveName;
	private String _actionRoundaboutDefault;
	private String _actionRoundaboutName;
	private String _actionContinueDefault;
	private String _actionContinueName;
	private String _actionKeepDefault;
	private String _actionKeepName;
	private String _actionTurnDefault;
	private String _actionTurnName;
	private String[] _numerals;
	private String[] _turnManeuvers;

	public InstructionTranslator(LanguageResources resources) throws Exception
	{
		_resources = resources;

		_directions = new String[8];
		_directions[0] = _resources.getTranslation("instructions.directions.north");
		_directions[1] = _resources.getTranslation("instructions.directions.northeast");
		_directions[2] = _resources.getTranslation("instructions.directions.east");
		_directions[3] = _resources.getTranslation("instructions.directions.southeast");
		_directions[4] = _resources.getTranslation("instructions.directions.south");
		_directions[5] = _resources.getTranslation("instructions.directions.southwest");
		_directions[6] = _resources.getTranslation("instructions.directions.west");
		_directions[7] = _resources.getTranslation("instructions.directions.northwest");

		_turnManeuvers = new String[10];
		_turnManeuvers[0] = _resources.getTranslation("instructions.turn_maneuvers.left");
		_turnManeuvers[1] = _resources.getTranslation("instructions.turn_maneuvers.right");
		_turnManeuvers[2] = _resources.getTranslation("instructions.turn_maneuvers.sharp_left");
		_turnManeuvers[3] = _resources.getTranslation("instructions.turn_maneuvers.sharp_right");
		_turnManeuvers[4] = _resources.getTranslation("instructions.turn_maneuvers.slight_left");
		_turnManeuvers[5] = _resources.getTranslation("instructions.turn_maneuvers.slight_right");
		_turnManeuvers[6] = _resources.getTranslation("instructions.turn_maneuvers.straight");
		_turnManeuvers[7] = _resources.getTranslation("instructions.turn_maneuvers.uturn");
		_turnManeuvers[8] = _resources.getTranslation("instructions.turn_maneuvers.left");
		_turnManeuvers[9] = _resources.getTranslation("instructions.turn_maneuvers.right");

		_numerals = new String[11];
		for (int i = 1; i<=10; i++)
			_numerals[i] = _resources.getTranslation("instructions.numerals."+Integer.toString(i));

		_actionDepartDefault = _resources.getTranslation("instructions.actions.depart.default.default");
		_actionDepartName = _resources.getTranslation("instructions.actions.depart.default.name");
		_actionContinueDefault = _resources.getTranslation("instructions.actions.continue.default.default");
		_actionContinueName = _resources.getTranslation("instructions.actions.continue.default.name");
		_actionKeepDefault = _resources.getTranslation("instructions.actions.keep.default.default");
		_actionKeepName = _resources.getTranslation("instructions.actions.keep.default.name");
		_actionTurnDefault = _resources.getTranslation("instructions.actions.turn.default.default");
		_actionTurnName = _resources.getTranslation("instructions.actions.turn.default.name");
		_actionRoundaboutDefault = _resources.getTranslation("instructions.actions.roundabout.default.exit.default");
		_actionRoundaboutName = _resources.getTranslation("instructions.actions.roundabout.default.exit.name");
		
		_actionArriveDefault = new String[4];
		_actionArriveName = new String[4];
		_actionArriveDefault[0] = _resources.getTranslation("instructions.actions.arrive.default.default");
		_actionArriveDefault[1] = _resources.getTranslation("instructions.actions.arrive.left.default");
		_actionArriveDefault[2] = _resources.getTranslation("instructions.actions.arrive.right.default");
		_actionArriveDefault[3] = _resources.getTranslation("instructions.actions.arrive.straight.default");
		_actionArriveName[0] = _resources.getTranslation("instructions.actions.arrive.default.name");
		_actionArriveName[1] = _resources.getTranslation("instructions.actions.arrive.left.name");
		_actionArriveName[2] = _resources.getTranslation("instructions.actions.arrive.right.name");
		_actionArriveName[3] = _resources.getTranslation("instructions.actions.arrive.straight.name");
	}

	public String getContinue(InstructionType type, String wayName)
	{
		boolean isWayNull = Helper.isEmpty(wayName);
		String str = isWayNull ? _actionContinueDefault: _actionContinueName;

		if (isWayNull)
			return str.replace("{turn_maneuver}",  _turnManeuvers[getTurnManeuver(type)]);
		else 
			return str.replace("{turn_maneuver}", _turnManeuvers[getTurnManeuver(type)]).replace("{way_name}", wayName);
	}

	public String getTurn(InstructionType type, String wayName)
	{
		boolean isWayNull = Helper.isEmpty(wayName);
		String str = isWayNull ? _actionTurnDefault: _actionTurnName;

		if (isWayNull)
			return str.replace("{turn_maneuver}",  _turnManeuvers[getTurnManeuver(type)]);
		else 
			return str.replace("{turn_maneuver}", _turnManeuvers[getTurnManeuver(type)]).replace("{way_name}", wayName);
	}

	public String getKeep(InstructionType type, String wayName)
	{
		boolean isWayNull = Helper.isEmpty(wayName);
		String str = isWayNull ? _actionKeepDefault: _actionKeepName;

		if (isWayNull)
			return str.replace("{turn_maneuver}",  _turnManeuvers[getTurnManeuver(type)]);
		else
			return str.replace("{turn_maneuver}", _turnManeuvers[getTurnManeuver(type)]).replace("{way_name}", wayName);
	}

	public String getRoundabout(int exitNumber, String wayName)
	{
		boolean isWayNull = Helper.isEmpty(wayName);
		String str = isWayNull ? _actionRoundaboutDefault: _actionRoundaboutName;
		boolean isExitNull = (exitNumber == 0);
		boolean highNumber = false;

		// We need to check if the exit number is greater than 10, as that is the most we have in the n-th representation
		highNumber = (exitNumber > _numerals.length-1);

		//If there was an error in finding the exit number, return "UNKNOWN". If there is no way name, don't return a way name
		if(isExitNull)
			str = str.replace("{exit_number}",  "UNKNOWN");
		else {
			String numeral = "";
			if(highNumber) {
				// if it is a high number which is very rare, then we dont use the numeral representation, just the
				// number itself
				// Converting to the th is too complicated due to exceptions and the position of the "th"

				numeral = Integer.toString(exitNumber);
			} else {
				numeral = _numerals[exitNumber];
			}

			str = str.replace("{exit_number}", numeral);
		}
		if (isWayNull)
			return str;
		else 
			return str.replace("{way_name}", wayName);
	}

	public String getDepart(CardinalDirection direction, String wayName) throws Exception
	{
		boolean isWayNull = Helper.isEmpty(wayName);
		String str = isWayNull ? _actionDepartDefault: _actionDepartName;

		if (isWayNull)
			return str.replace("{direction}", _directions[direction.ordinal()]);
		else 
			return str.replace("{direction}", _directions[direction.ordinal()]).replace("{way_name}", wayName);
	}
	
	public String getArrive(ArrivalDirection direction, String wayName) throws Exception
	{
		boolean isWayNull = Helper.isEmpty(wayName);
		
		String str = isWayNull ? _actionArriveDefault[direction.ordinal()]: _actionArriveName[direction.ordinal()];
		
		if (isWayNull)
			return str;
		else 
			return str.replace("{way_name}", wayName);
	}

	private int getTurnManeuver(InstructionType type)
	{
	    switch (type){
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
		//	_turnManeuvers[7] = _resources.getTranslation("instructions.turn_maneuvers.uturn");
		return 0;
	}
}
