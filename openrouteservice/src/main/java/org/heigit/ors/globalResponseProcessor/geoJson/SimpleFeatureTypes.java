/*
 *
 *  *
 *  *  *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *  *  *
 *  *  *   http://www.giscience.uni-hd.de
 *  *  *   http://www.heigit.org
 *  *  *
 *  *  *  under one or more contributor license agreements. See the NOTICE file
 *  *  *  distributed with this work for additional information regarding copyright
 *  *  *  ownership. The GIScience licenses this file to you under the Apache License,
 *  *  *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  *  *  with the License. You may obtain a copy of the License at
 *  *  *
 *  *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  *  Unless required by applicable law or agreed to in writing, software
 *  *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  *  See the License for the specific language governing permissions and
 *  *  *  limitations under the License.
 *  *
 *
 */

package heigit.ors.globalResponseProcessor.geoJson;


import com.vividsolutions.jts.geom.LineString;
import heigit.ors.services.routing.RoutingServiceSettings;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * {@link SimpleFeatureTypes} defines {@link SimpleFeatureType} for each Request that will be exported as GeoJSON.
 * The class is only accessible through classes in the same package.
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
class SimpleFeatureTypes {
    private RouteFeatureType type;

    public enum RouteFeatureType {
        routeFeature
    }

    /**
     * The constructor itself only sets the {@link RouteFeatureType} according to the given "type" parameter.
     *
     * @param type The input must be a {@link RouteFeatureType} according to the enum.
     */
    SimpleFeatureTypes(RouteFeatureType type) {
        this.type = type;
    }

    /**
     * The function creates a {@link SimpleFeatureType} according to the given {@link RouteFeatureType}.
     *
     * @return The return is a {@link SimpleFeatureType}.
     */
    public SimpleFeatureType create() {
        if (type == RouteFeatureType.routeFeature) {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName(RoutingServiceSettings.getRoutingName());
            builder.add("geometry", LineString.class);
            return builder.buildFeatureType();
        }
        return null;
    }
}




