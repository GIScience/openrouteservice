/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper GmbH licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.util;

/**
 * @author Hendrik Leuschner
 */
public class ORSParameters {
    /* Parameters with an 'INIT' prefix are used as defaults and/or are configured at start.*/
    static final String ROUTING_INIT_PREFIX = "routing.";

    /**
     * Parameters that can be used for algorithm.
     */

    /**
     * Properties for routing with contraction hierarchies speedup
     */
    public static final class Core {
        public static final String PREPARE = "prepare.core.";
        /**
         * This property name in HintsMap configures at runtime if CH routing should be ignored.
         */
        public static final String DISABLE = "core.disable";
        /**
         * This property name configures at start if the DISABLE parameter can have an effect.
         */
        public static final String INIT_DISABLING_ALLOWED = ROUTING_INIT_PREFIX + "core.disabling_allowed";
        /**
         * The property name in HintsMap if heading should be used for CH regardless of the possible
         * routing errors.
         */
        public static final String FORCE_HEADING = "core.force_heading";
    }

    /**
     * Properties for routing with landmark speedup
     */
    public static final class CoreLandmark {
        public static final String PREPARE = "prepare.corelm.";
        /**
         * This property name in HintsMap configures at runtime if CH routing should be ignored.
         */
        public static final String LMSETS = PREPARE + "lmsets";
        /**
         * This property name in HintsMap configures at runtime if CH routing should be ignored.
         */
        public static final String DISABLE = "corelm.disable";
        /**
         * Specifies how many active landmarks should be used when routing
         */
        public static final String ACTIVE_COUNT = ROUTING_INIT_PREFIX + "corelm.active_landmarks";
        /**
         * Default for active count
         */
        public static final String ACTIVE_COUNT_DEFAULT = ROUTING_INIT_PREFIX + ACTIVE_COUNT;
        /**
         * Specifies how many landmarks should be created
         */
        public static final String COUNT = PREPARE + "landmarks";
        /**
         * This property name configures at start if the DISABLE parameter can have an effect.
         */
        public static final String INIT_DISABLING_ALLOWED = ROUTING_INIT_PREFIX + "corelm.disabling_allowed";
    }


}
