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

public enum InstructionType {

    //Keep in sync with documentation: instruction-types.md

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
    PT_ENTER,               /*14*/
    PT_TRANSFER,              /*15*/
    PT_EXIT,                 /*16*/
    UNKNOWN                 /*17*/;

    public boolean isSlightLeftOrRight() {
        return this == TURN_SLIGHT_RIGHT || this == TURN_SLIGHT_LEFT;
    }
}
