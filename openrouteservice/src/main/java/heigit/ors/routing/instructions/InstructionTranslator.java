/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

		_turnManeuvers = new String[8];
		_turnManeuvers[0] = _resources.getTranslation("instructions.turn_maneuvers.left");
		_turnManeuvers[1] = _resources.getTranslation("instructions.turn_maneuvers.right");
		_turnManeuvers[2] = _resources.getTranslation("instructions.turn_maneuvers.sharp_left");
		_turnManeuvers[3] = _resources.getTranslation("instructions.turn_maneuvers.sharp_right");
		_turnManeuvers[4] = _resources.getTranslation("instructions.turn_maneuvers.slight_left");
		_turnManeuvers[5] = _resources.getTranslation("instructions.turn_maneuvers.slight_right");
		_turnManeuvers[6] = _resources.getTranslation("instructions.turn_maneuvers.straight");
		_turnManeuvers[7] = _resources.getTranslation("instructions.turn_maneuvers.uturn");

		_numerals = new String[11];
		for (int i = 1; i<=10; i++)
			_numerals[i] = _resources.getTranslation("instructions.numerals."+Integer.toString(i));

		_actionDepartDefault = _resources.getTranslation("instructions.actions.depart.default.default");
		_actionDepartName = _resources.getTranslation("instructions.actions.depart.default.name");
		_actionContinueDefault = _resources.getTranslation("instructions.actions.continue.default.default");
		_actionContinueName = _resources.getTranslation("instructions.actions.continue.default.name");
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

	public String getRoundabout(int exitNumber, String wayName)
	{
		boolean isWayNull = Helper.isEmpty(wayName);
		String str = isWayNull ? _actionRoundaboutDefault: _actionRoundaboutName;
		boolean isExitNull = (exitNumber == 0);

		//If there was an error in finding the exit number, return "UNKNOWN". If there is no way name, don't return a way name
		if(isExitNull)
			str = str.replace("{exit_number}",  "UNKNOWN");
		else
			str = str.replace("{exit_number}",  _numerals[exitNumber]);
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
		if (type == InstructionType.TURN_LEFT)
			return 0;
		else if (type == InstructionType.TURN_RIGHT)
			return 1;
		else if (type == InstructionType.TURN_SHARP_LEFT)
			return 2;
		else if (type == InstructionType.TURN_SHARP_RIGHT)
			return 3;
		else if (type == InstructionType.TURN_SLIGHT_LEFT)
			return 4;
		else if (type == InstructionType.TURN_SLIGHT_RIGHT)
			return 5;
		else if (type == InstructionType.CONTINUE)
			return 6;
		//TODO
		//	_turnManeuvers[7] = _resources.getTranslation("instructions.turn_maneuvers.uturn");

		return 0;
	}
}
