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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryType;


import static heigit.ors.globalResponseProcessor.geoJson.SimpleFeatureTypes.RouteFeatureType;


public class SimpleFeatureTypesTest {
    private static SimpleFeatureType simpleFeatureType;


    @BeforeClass
    public static void setUp() {
        simpleFeatureType = new SimpleFeatureTypes(RouteFeatureType.routeFeature).create();
    }

    @Test
    public void testCreateRouteFeatureType() {
        Assert.assertEquals(SimpleFeatureTypeImpl.class, simpleFeatureType.getClass());
        Assert.assertNotNull(simpleFeatureType.getName());
        Assert.assertNotSame(-1, simpleFeatureType.indexOf("geometry"));
        GeometryType type = simpleFeatureType.getGeometryDescriptor().getType();
        Assert.assertEquals(LineString.class.getName(), type.getBinding().getName());
    }
}
