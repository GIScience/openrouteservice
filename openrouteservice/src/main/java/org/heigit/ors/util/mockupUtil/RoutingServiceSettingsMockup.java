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

package heigit.ors.util.mockupUtil;

import heigit.ors.services.routing.RoutingServiceSettings;

/**
 * This is a {@link heigit.ors.services.routing.RoutingServiceSettings} Mockup-Class, used in junit tests and wherever needed.
 * The mockup is intended as generic as possible. Make sure you check the element out using debug before integrating it.
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class RoutingServiceSettingsMockup {
    private static RoutingServiceSettings routingServiceSettingsMockup = new RoutingServiceSettings();
    // Create a RoutingServiceSettings mockup

    public RoutingServiceSettingsMockup() {
        routingServiceSettingsMockup = new RoutingServiceSettings();
    }

    public RoutingServiceSettings create() {
        RoutingServiceSettings.loadFromFile(RoutingServiceSettings.getSourceFile());
        return routingServiceSettingsMockup;
    }
}
