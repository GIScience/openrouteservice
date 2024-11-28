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
package org.heigit.ors.util;

import com.graphhopper.routing.weighting.Weighting;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.graphhopper.util.Helper.toLowerCase;

public class FileUtility {
    private FileUtility() {
    }

    public static String readResource(String resourcePath) throws IOException {
        URL resource = FileUtility.class.getResource(resourcePath);
        InputStream ris = resource.openStream();
        String result = StreamUtility.readStream(ris);
        ris.close();

        return result;
    }

    public static String weightingToFileName(Weighting w) {
        return toLowerCase(w.toString()).replace("|", "_");
    }

}
