package heigit.ors.util;

public class ArraysUtility {
	public static String toString(int[] a, String separator) {
		if (a == null)
			return null;
		int iMax = a.length - 1;
		if (iMax == -1)
			return "";

		StringBuilder b = new StringBuilder();

		for (int i = 0; ; i++) {
			b.append(a[i]);
			if (i == iMax)
				return b.toString();

			b.append(separator);
		}
	}
}
