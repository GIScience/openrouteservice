/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. The GIScience licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.storages;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BordersGraphStorageTest {

    private final BordersGraphStorage _storage;

    public BordersGraphStorageTest() {
        _storage = new BordersGraphStorage();
        _storage.init();
        _storage.create(1);
    }

    @Test
    public void TestItemCreation() {
        _storage.setEdgeValue(1, (short)1, (short)2, (short)3);

        assertEquals(_storage.getEdgeValue(1, BordersGraphStorage.Property.TYPE), 1);
        assertEquals(_storage.getEdgeValue(1, BordersGraphStorage.Property.START), 2);
        assertEquals(_storage.getEdgeValue(1, BordersGraphStorage.Property.END), 3);
    }
}
