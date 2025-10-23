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
package org.heigit.ors.routing.parameters;

import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;

import java.util.List;

@Setter
@Getter
public class VehicleParameters extends ProfileParameters {
    public static final double UNSET_VALUE = 0.0;
    private double length = UNSET_VALUE;
    private double height = UNSET_VALUE;
    private double width = UNSET_VALUE;
    private double weight = UNSET_VALUE;
    private double axleload = UNSET_VALUE;
    private int loadCharacteristics = VehicleLoadCharacteristicsFlags.NONE;

    public boolean hasLength() {
        return length > UNSET_VALUE;
    }

    public boolean hasHeight() {
        return height > UNSET_VALUE;
    }

    public boolean hasWidth() {
        return width > UNSET_VALUE;
    }

    public boolean hasWeight() {
        return weight > UNSET_VALUE;
    }

    public boolean hasAxleload() {
        return axleload > UNSET_VALUE;
    }

    public boolean hasLoadCharacteristics() {
        return loadCharacteristics != VehicleLoadCharacteristicsFlags.NONE;
    }

    public boolean hasAttributes() {
        return hasLength() || hasHeight() || hasWidth() || hasWeight() || hasAxleload() || hasLoadCharacteristics();
    }

    @Override
    public List<String> getValidRestrictions() {
        List<String> valid = super.getValidRestrictions();
        valid.add("height");
        valid.add("length");
        valid.add("width");
        valid.add("weight");
        valid.add("hazmat");
        valid.add("axleload");
        return valid;
    }
}
