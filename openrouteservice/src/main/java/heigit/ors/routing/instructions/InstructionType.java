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

public enum InstructionType 
{
    TURN_LEFT,              /*0*/
    TURN_RIGHT,             /*1*/
    TURN_SHARP_LEFT,        /*2*/
    TURN_SHARP_RIGHT,       /*3*/
    TURN_SLIGHT_LEFT,       /*4*/
    TURN_SLIGHT_RIGHT,      /*5*/
    CONTINUE,               /*6*/
    ENTER_ROUNDABOUT,       /*7*/
    EXIT_ROUNDABOUT,        /*8*/
    UTURN,                  /*9*/
    FINISH,                 /*10*/
	DEPART,                 /*11*/
    KEEP_LEFT,              /*12*/
    KEEP_RIGHT,             /*13*/
    UNKNOWN                 /*14*/
}
