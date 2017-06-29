package heigit.ors.matrix;

/*
 *		t0		t1		t2 
 * s0| (t00,d00) (t01,d01)...
 * s1| (t10,d10) ...
 * s2|
 * 
 * -->
 * 
 * [t00, d00, t01, d01, ..., t10, d10, t11, d11, ..., tNM, dNM]
 * dimCol = M
 * dimRow = N
 */
public class MatrixResponse {
	private final float[] mat0;
	private float[] mat1;
	private int dimCol;
	private int dimRow;

	/**
	 * 
	 * @param dimCol
	 *            Number of destinations
	 * @param dimRow
	 *            Number of sources
	 * @param isTimeAndDistance
	 *            True if the matrix will contain both time and distance
	 *            information for each s-d pair. False if only time or only
	 *            distance.
	 */
	public MatrixResponse(int dimCol, int dimRow, boolean isTimeAndDistance) {
		this.dimCol = dimCol;
		this.dimRow = dimRow;
		mat0 = new float[dimCol * dimRow];
		if (isTimeAndDistance)
			mat1 = new float[dimCol * dimRow];
	}

	public MatrixResponse(float[] times) {
		mat0 = times;
	}

	public MatrixResponse(float[] times, float[] distances) {
		mat0 = times;
		mat1 = distances;
	}

	public float getTime(int src, int dest) {
		if (src > dimCol || dest > dimRow)
			throw new IllegalArgumentException("Source or destination out of range");
		return mat0[src * dimCol + dest];
	}

	public float getDistance(int src, int dest) {
		if (mat1 == null)
			throw new IllegalStateException("MatrixResponse not initialized for distance matrix");
		if (src > dimCol || dest > dimRow)
			throw new IllegalArgumentException("Source or destination out of range");
		return mat1[src * dimCol + dest];
	}

	public float[] getMat1() {
		return mat1;
	}

	public float[] getMat0() {
		return mat0;
	}

}
