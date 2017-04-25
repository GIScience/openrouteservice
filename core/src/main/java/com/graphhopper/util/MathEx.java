package com.graphhopper.util;

public class MathEx {
	private final static double HALF_PI = Math.PI / 2.0;

	// Atan2 static caches
	private static final int SIZE_AC = 100000;
	private static final int SIZE_AR = SIZE_AC + 1;

	private static final double Atan2[] = new double[SIZE_AR];
	private static final double Atan2_PM[] = new double[SIZE_AR];
	private static final double Atan2_MP[] = new double[SIZE_AR];
	private static final double Atan2_MM[] = new double[SIZE_AR];

	private static final double Atan2_R[] = new double[SIZE_AR];
	private static final double Atan2_RPM[] = new double[SIZE_AR];
	private static final double Atan2_RMP[] = new double[SIZE_AR];
	private static final double Atan2_RMM[] = new double[SIZE_AR];

	static {
		float pi = (float) Math.PI;
		float pi_h = pi / 2;

		for (int i = 0; i <= SIZE_AC; i++) {
			double d = (double) i / SIZE_AC;
			double x = 1;
			double y = x * d;
			double v = Math.atan2(y, x);
			Atan2[i] = v;
			Atan2_PM[i] = pi - v;
			Atan2_MP[i] = -v;
			Atan2_MM[i] = -pi + v;

			Atan2_R[i] = pi_h - v;
			Atan2_RPM[i] = pi_h + v;
			Atan2_RMP[i] = -pi_h + v;
			Atan2_RMM[i] = -pi_h - v;
		}
	}

	/*
	 *  
     *  Icecore's atan2 ( http://www.java-gaming.org/topics/extremely-fast-atan2/36467/msg/346145/view.html#msg346145 )
     */
  	public static final double atan2(double y, double x) {
		if (y < 0) {
			if (x < 0) {
				//(y < x) because == (-y > -x)
				if (y < x) {
					return Atan2_RMM[(int) (x / y * SIZE_AC)];
				} else {
					return Atan2_MM[(int) (y / x * SIZE_AC)];
				}
			} else {
				y = -y;
				if (y > x) {
					return Atan2_RMP[(int) (x / y * SIZE_AC)];
				} else {
					return Atan2_MP[(int) (y / x * SIZE_AC)];
				}
			}
		} else {
			if (x < 0) {
				x = -x;
				if (y > x) {
					return Atan2_RPM[(int) (x / y * SIZE_AC)];
				} else {
					return Atan2_PM[(int) (y / x * SIZE_AC)];
				}
			} else {
				if (y > x) {
					return Atan2_R[(int) (x / y * SIZE_AC)];
				} else {
					return Atan2[(int) (y / x * SIZE_AC)];
				}
			}
		}
	}

	public static double asin(double x)
	{
		boolean negate = x < 0;
		x = Math.abs(x);
		double y1 = x * ( -.0170881256 + ( x * ( .0066700901 + ( x * -.0012624911 ) ) ) );
		double y2 = x * ( -.0501743046 + ( x * ( .0308918810 + y1 ) ) );
		double y = 1.5707963050 + ( x * ( -.2145988016 + ( x * ( .0889789874 + y2 ) ) ) );
		y = HALF_PI - (Math.sqrt( 1.0 - x ) * y);

		if (negate)
			y = -y;

		return y;
	}
}
