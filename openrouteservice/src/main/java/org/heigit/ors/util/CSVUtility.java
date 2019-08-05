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

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Helper class for reading data from a CSV file. Based on code from
 * https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
 */
public class CSVUtility {
    private static final Logger LOGGER = Logger.getLogger(CSVUtility.class);

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
    private static final boolean HAS_HEADER = true;

    public static ArrayList<ArrayList<String>> readFile(String file) {
        return readFile(file, DEFAULT_SEPARATOR, DEFAULT_QUOTE, HAS_HEADER);
    }

    /**
     * Read data from CSV file using the provided filename, seperator, quote character and whether there is a header
     *
     * @param file          CSV file to read from
     * @param seperator     Character used to seperate fields
     * @param quote         Character used to enclose strings
     * @param header        Whether to ignore the first row of the CSV
     * @return              An ArrayList (rows) of ArrayLists (columns values)
     */
    public static ArrayList<ArrayList<String>> readFile(String file, char seperator, char quote, boolean header) {
        // Open the CSV file
        String ln = "";

        ArrayList<ArrayList<String>> lines = new ArrayList<>();
        boolean headerRead = false;
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            while ((ln = br.readLine()) != null) {
                if(header && lines.size() == 0 && !headerRead) {
                    headerRead=true;
                    continue;
                }

                lines.add(parseLine(ln));
            }
        } catch (IOException ioe) {
            LOGGER.error("Could not read from file: " + file);
        }

        return lines;
    }

    private static ArrayList<String> parseLine(String csvLine) {
        return parseLine(csvLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    private static ArrayList<String> parseLine(String csvLine, char seperator) {
        return parseLine(csvLine, seperator, DEFAULT_QUOTE);
    }

    /**
     * Parse a line of text read from a CSV file. The line is split into values.
     *
     * @param csvLine       The line of the CSV file
     * @param separator     The seperator between values
     * @param customQuote   The character enclosing strings
     * @return
     */
    private static ArrayList<String> parseLine(String csvLine, char separator, char customQuote) {

        ArrayList<String> result = new ArrayList<>();

        //if empty, return!
        if (csvLine == null && csvLine.isEmpty()) {
            return result;
        }

        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }

        if (separator == ' ') {
            separator = DEFAULT_SEPARATOR;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = csvLine.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }

                }
            } else {
                if (ch == customQuote) {

                    inQuotes = true;

                    if (chars[0] != '"' && customQuote != '\"') {
                        curVal.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separator) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
    }
}
