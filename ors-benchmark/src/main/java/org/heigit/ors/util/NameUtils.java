package org.heigit.ors.util;

import java.io.File;

public class NameUtils {
    private NameUtils() {
        // Hide implicit constructor
    }

    public static String getFileNameWithoutExtension(String filePath) {
        if (filePath == null) {
            return null;
        }
        String fileName = new File(filePath).getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }
}
