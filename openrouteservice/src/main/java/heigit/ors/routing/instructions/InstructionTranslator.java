/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing.instructions;

import com.graphhopper.util.Helper;

import heigit.ors.localization.LanguageResources;
import heigit.ors.util.CardinalDirection;

public class InstructionTranslator 
{
	private LanguageResources _resources;

	private String[] _directions;
	private String _actionDepartDefault;
	private String _actionDepartName;
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

		if (isWayNull)
			return str.replace("{exit_number}",  _numerals[exitNumber]);
		else 
			return str.replace("{exit_number}", _numerals[exitNumber]).replace("{way_name}", wayName);
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
