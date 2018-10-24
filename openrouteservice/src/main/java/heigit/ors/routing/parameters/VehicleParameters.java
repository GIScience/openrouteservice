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
package heigit.ors.routing.parameters;

import heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;

public class VehicleParameters extends ProfileParameters {
	private double _length = 0.0;
	private double _height = 0.0;
	private double _width = 0.0;
	private double _weight = 0.0;
	private double _axleload = 0.0;

	private int _characteristics = VehicleLoadCharacteristicsFlags.NONE;

	public VehicleParameters() {}

	public double getLength() {
		return _length;
	}

	public void setLength(double length) {
		_length = length;
	}

	public double getHeight() {
		return _height;
	}

	public void setHeight(double height) {
		_height = height;
	}

	public double getWidth() {
		return _width;
	}

	public void setWidth(double width) {
		_width = width;
	}

	public double getWeight() {
		return _weight;
	}

	public void setWeight(double weight) {
		_weight = weight;
	}

	public double getAxleload() {
		return _axleload;
	}

	public void setAxleload(double axleload) {
		_axleload = axleload;
	}

	public int getLoadCharacteristics() {
		return _characteristics;
	}

	public void setLoadCharacteristics(int characteristics) {
		_characteristics = characteristics;
	}
	
	public boolean hasAttributes() {
		return _height > 0.0 || _length > 0.0 || _width > 0.0 || _weight > 0.0 || _axleload > 0.0 || _characteristics != 0;
	}
}
