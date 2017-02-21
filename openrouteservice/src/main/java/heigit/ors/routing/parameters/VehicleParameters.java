package heigit.ors.routing.parameters;

import heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;

public class VehicleParameters extends ProfileParameters {
	private double _length = 0.0;
	private double _height = 0.0;
	private double _width = 0.0;
	private double _weight = 0.0;
	private double _axleload = 0.0;

	private int _characteristics = VehicleLoadCharacteristicsFlags.NONE;

	public VehicleParameters()
	{

	}

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

	public void setWeight(double _weight) {
		this._weight = _weight;
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
	
	public boolean hasAttributes()
	{
		return _height >0.0 || _length > 0.0 || _width > 0.0 || _weight >0.0 || _characteristics != 0;
	}
}
