
package org.freeopenls.location.search;

/**
 * <p><b>Title: Levenshtein</b></p>
 * <p><b>Description:</b> Class for Levenshtein<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-05-10
 */
public class Levenshtein {
	
	/**
	 * ********************************<br>
	 * * Compute Levenshtein distance *<br>
	 * ********************************<br>
	 * <br>
	 * Levenshtein distance (LD) is a measure of the similarity between two<br>
	 * strings, which we will refer to as the source string (s) and the target<br>
	 * string (t). The distance is the number of deletions, insertions, or<br>
	 * substitutions required to transform s into t. For example,<br>
	 * <br>
	 * If s is "test" and t is "test", then LD(s,t) = 0, because no<br>
	 * transformations are needed. The strings are already identical. If s is<br>
	 * "test" and t is "tent", then LD(s,t) = 1, because one substitution<br>
	 * (change "s" to "n") is sufficient to transform s into t. The greater the<br>
	 * Levenshtein distance, the more different the strings are.<br>
	 *
	 * @param s
     *			String s
     * @param t
     * 			String t
	 */
	public static int Distance(String s, String t) {
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		int cost; // cost

		// Step 1
		/*
		 * Set n to be the length of s. Set m to be the length of t. If n = 0,
		 * return m and exit. If m = 0, return n and exit. Construct a matrix
		 * containing 0..m rows and 0..n columns.
		 */

		n = s.length();
		m = t.length();
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];

		// Step 2
		/*
		 * Initialize the first row to 0..n. Initialize the first column to
		 * 0..m.
		 */
		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}

		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}

		// Step 3
		/* Examine each character of s (i from 1 to n). */
		for (i = 1; i <= n; i++) {

			s_i = s.charAt(i - 1);

			// Step 4
			/* Examine each character of t (j from 1 to m). */
			for (j = 1; j <= m; j++) {

				t_j = t.charAt(j - 1);

				// Step 5
				/*
				 * If s[i] equals t[j], the cost is 0. If s[i] doesn't equal
				 * t[j], the cost is 1.
				 */

				if (s_i == t_j) {
					cost = 0;
				} else {
					cost = 1;
				}

				// Step 6
				/*
				 * Set cell d[i,j] of the matrix equal to the minimum of: a. The
				 * cell immediately above plus 1: d[i-1,j] + 1. b. The cell
				 * immediately to the left plus 1: d[i,j-1] + 1. c. The cell
				 * diagonally above and to the left plus the cost: d[i-1,j-1] +
				 * cost.
				 */
				d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1,
						d[i - 1][j - 1] + cost);

			}

		}

		// Step 7
		/*
		 * After the iteration steps (3, 4, 5, 6) are complete, the distance is
		 * found in cell d[n,m].
		 */
		return d[n][m];
	}
	
	/**
	 * *******************************
	 * * Get minimum of three values *
	 * *******************************
	 */
	private static int Minimum(int a, int b, int c) {
		int mi = a;
		if (b < mi) {
			mi = b;
		}
		if (c < mi) {
			mi = c;
		}
		return mi;
	}
}