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
package heigit.ors.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

import org.apache.commons.io.FileUtils;

public class FileUtility {
	public static Path getResourcesPath()
	{
		File classFile = new File(FileUtility.class.getProtectionDomain().getCodeSource().getLocation().getFile());
		String classPath = classFile.getAbsolutePath();
		String classesPath = classPath.substring(0, classPath.indexOf("classes") + "classes".length());
		return Paths.get(classesPath, "resources");
	}

	public static Boolean isAbsolutePath(String path)
	{
		Path path2 = Paths.get(path);
		return path2.isAbsolute();
	}

	public static String readFile(String fileName, int bufferSize) throws IOException {
		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);
		String result = StreamUtility.readStream(fis);
		fis.close();

		return result;
	}

	public static String readFile(String fileName, int bufferSize, String encoding) throws IOException {
		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);
		String result = StreamUtility.readStream(fis, encoding);
		fis.close();

		return result;
	}

	public static String readFile(File file, int bufferSize) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		String result = StreamUtility.readStream(fis);
		fis.close();

		return result;
	}

	public static void writeFile(String fileName, String encoding, String content) throws IOException
	{
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName), encoding));
		out.append(content);
		out.flush();
		out.close();
	}

	public static String readFile(String fileName) throws IOException {
		return readFile(fileName, 8192);
	}

	public static String readFile(String fileName, String encoding) throws IOException {
		return readFile(fileName, 8192, encoding);
	}

	public static String combinePaths(String[] paths) {
		if (paths.length == 0) {
			return "";
		}

		File combined = new File(paths[0]);

		int i = 1;
		while (i < paths.length) {
			combined = new File(combined, paths[i]);
			++i;
		}

		return combined.getPath();
	}

	public static void makeDirectory(String directory) throws Exception {
		File dir = new File(directory);
		if (!dir.exists()) {
			try {
				FileUtils.forceMkdir(dir);
			} catch (SecurityException se) {
				// handle it
			}

			if (!dir.exists())
				throw new Exception("Unable to create directory - " + directory);
		}
	}

	public static String getMd5OfFile(String filePath)
	{
		String returnVal = "";
		try 
		{
			InputStream   input   = new FileInputStream(filePath); 
			byte[]        buffer  = new byte[1024];
			MessageDigest md5Hash = MessageDigest.getInstance("MD5");
			int           numRead = 0;
			while (numRead != -1)
			{
				numRead = input.read(buffer);
				if (numRead > 0)
				{
					md5Hash.update(buffer, 0, numRead);
				}
			}
			input.close();

			byte [] md5Bytes = md5Hash.digest();
			for (int i=0; i < md5Bytes.length; i++)
			{
				returnVal += Integer.toString( ( md5Bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
			}
		} 
		catch(Throwable t) {t.printStackTrace();}

		return returnVal.toUpperCase();
	}

	public static String getExtension(String fileName)
	{
		String extension = "";

		int i = fileName.lastIndexOf('.');
		int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (i > p) {
			extension = fileName.substring(i);
		}

		return extension;
	}

	public static String getFileName(URL extUrl) {
		// URL:
		// "http://photosaaaaa.net/photos-ak-snc1/v315/224/13/659629384/s659629384_752969_4472.jpg"
		String filename = "";
		// PATH:
		// /photos-ak-snc1/v315/224/13/659629384/s659629384_752969_4472.jpg
		String path = extUrl.getPath();
		// Checks for both forward and/or backslash
		// NOTE:**While backslashes are not supported in URL's
		// most browsers will autoreplace them with forward slashes
		// So technically if you're parsing an html page you could run into
		// a backslash , so i'm accounting for them here;
		String[] pathContents = path.split("[\\\\/]");
		if (pathContents != null) {
			int pathContentsLength = pathContents.length;
			System.out.println("Path Contents Length: " + pathContentsLength);
			for (int i = 0; i < pathContents.length; i++) {
				System.out.println("Path " + i + ": " + pathContents[i]);
			}
			// lastPart: s659629384_752969_4472.jpg
			String lastPart = pathContents[pathContentsLength - 1];
			String[] lastPartContents = lastPart.split("\\.");
			if (lastPartContents != null && lastPartContents.length > 1) {
				int lastPartContentLength = lastPartContents.length;
				System.out.println("Last Part Length: " + lastPartContentLength);
				// filenames can contain . , so we assume everything before
				// the last . is the name, everything after the last . is the
				// extension
				String name = "";
				for (int i = 0; i < lastPartContentLength; i++) {
					System.out.println("Last Part " + i + ": " + lastPartContents[i]);
					if (i < (lastPartContents.length - 1)) {
						name += lastPartContents[i];
						if (i < (lastPartContentLength - 2)) {
							name += ".";
						}
					}
				}
				String extension = lastPartContents[lastPartContentLength - 1];
				filename = name + "." + extension;
				System.out.println("Name: " + name);
				System.out.println("Extension: " + extension);
				System.out.println("Filename: " + filename);
			}
		}
		return filename;
	}

}
