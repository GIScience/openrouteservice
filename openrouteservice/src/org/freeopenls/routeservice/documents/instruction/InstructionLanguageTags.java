package org.freeopenls.routeservice.documents.instruction;

import com.graphhopper.util.Helper;

/**
 * <p>
 * <b>Title: InstructionLanguageTags</b>
 * </p>
 * <p>
 * <b>Description:</b> Class for Instruction-Language-Tags<br>
 * </p>
 * 
 * <p>
 * <b>Copyright:</b> Copyright (c) 2008 by Pascal Neis
 * </p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-11-13
 */
public class InstructionLanguageTags {

	/** Language **/
	public final String LANGUAGE_CODE;
	public final String LANGUAGE;

	/** general-words **/
	public final String ACTION_NR;
	public final String START;
	public final String FINSIH;
	public final String DRIVE;
	public final String GO;
	public final String TURN;

	/** Direction **/
	public final String DIRECTION_STRAIGHTFORWARD;
	public final String DIRECTION_LEFT;
	public final String DIRECTION_SHARP_LEFT;
	public final String DIRECTION_HALFLEFT;
	public final String DIRECTION_RIGHT;
	public final String DIRECTION_SHARP_RIGHT;
	public final String DIRECTION_HALFRIGHT;
	public final String[] DIRECTION_INITIAL_HEADING;

	/** Time **/
	public final String TIME_APPROX;
	public final String TIME_DAY;
	public final String TIME_HOUR;
	public final String TIME_MINUTE;
	public final String TIME_SECOND;

	/** Fill-words **/
	public final String FILLWORD_ON;
	public final String FILLWORD_FOR;
	public final String FILLWORD_BEFORE;
	public final String FILLWORD_AFTER;

	/**
	 * 
	 * @param LanguageCode
	 * @param Language
	 * @param ActionNr
	 * @param Start
	 * @param Finish
	 * @param Drive
	 * @param Go
	 * @param StraightForward
	 * @param Left
	 * @param HalfLeft
	 * @param Right
	 * @param HalfRight
	 * @param Approx
	 * @param Day
	 * @param Hour
	 * @param Minute
	 * @param Second
	 * @param On
	 * @param For
	 * @param Before
	 * @param After
	 */
	public InstructionLanguageTags(String LanguageCode, String Language, String ActionNr, String Start, String Finish,
			String Drive, String Go, String Turn, String StraightForward, String Left, String SharpLeft, String HalfLeft, String Right,String SharpRight,
			String HalfRight, String InitialHeading, String Approx, String Day, String Hour, String Minute, String Second, String On,
			String For, String Before, String After) {

		// Language
		LANGUAGE_CODE = LanguageCode;
		LANGUAGE = Language;

		// general-words
		ACTION_NR = ActionNr;
		START = Start;
		FINSIH = Finish;
		DRIVE = Drive;
		GO = Go;
		TURN = Turn;

		// Direction
		DIRECTION_STRAIGHTFORWARD = StraightForward;
		DIRECTION_LEFT = Left;
		DIRECTION_SHARP_LEFT = SharpLeft;
		DIRECTION_HALFLEFT = HalfLeft;
		DIRECTION_RIGHT = Right;
		DIRECTION_SHARP_RIGHT = SharpRight;
		DIRECTION_HALFRIGHT = HalfRight;
		
		if (Helper.isEmpty(InitialHeading))
			DIRECTION_INITIAL_HEADING = new String[] { "north", "northeast", "east", "southeast", "south", "southwest", "west", "northwest" };
		else
		{
			String[] strs = InitialHeading.split(",");
			DIRECTION_INITIAL_HEADING = new String[strs.length];
			
			for (int i = 0; i < strs.length; i++)
				DIRECTION_INITIAL_HEADING[i] = strs[i].trim();
		}

		// Time
		TIME_APPROX = Approx;
		TIME_DAY = Day;
		TIME_HOUR = Hour;
		TIME_MINUTE = Minute;
		TIME_SECOND = Second;

		// Fill-words
		FILLWORD_ON = On;
		FILLWORD_FOR = For;
		FILLWORD_BEFORE = Before;
		FILLWORD_AFTER = After;
	}
}
