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
package heigit.ors.routing;

public class WayPointBearing {
    private double _value;

    @Deprecated
    // MARQ24 - GHRequest does not support an additional deviation value - and it had never any effect...
    private double _deviation = 0.0;

    public WayPointBearing(double value, double deviation) {
        _value = value;
        _deviation = deviation;
    }

    public double getValue() {
        return _value == -1.0 ? Double.NaN : _value;
    }

    @Deprecated
    public double getDeviation() {
        return _deviation;
    }
}
