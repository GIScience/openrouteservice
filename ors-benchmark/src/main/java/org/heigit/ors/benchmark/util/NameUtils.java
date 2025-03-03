package org.heigit.ors.benchmark.util;

import java.io.File;

public class NameUtils {
    public static String getFileNameWithoutExtension(String filePath) {
        String fileName = new File(filePath).getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }


}
