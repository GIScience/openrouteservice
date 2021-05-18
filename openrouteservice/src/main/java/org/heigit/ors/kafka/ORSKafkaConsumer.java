/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://giscience.uni-hd.de
 *   http://heigit.org
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
package org.heigit.ors.kafka;

import java.util.ArrayList;
import java.util.List;

public class ORSKafkaConsumer {
    private List<ORSKafkaConsumerRunner> runners = new ArrayList<>();
    private static int enabledRunners = 0;

    public static int getEnabledRunners() {
        return enabledRunners;
    }

    public static boolean isEnabled() {
        return enabledRunners > 0;
    }

    public ORSKafkaConsumer(List<ORSKafkaConsumerConfiguration> configs) {
        for (ORSKafkaConsumerConfiguration c : configs) {
            ORSKafkaConsumerRunner runner = new ORSKafkaConsumerRunner(c);
            runners.add(runner);
            enabledRunners++;
        }
    }

    public void startConsumer() {
        for (ORSKafkaConsumerRunner runner : runners) {
            new Thread(runner).start();
        }
    }

    public void stopConsumer() {
        for (ORSKafkaConsumerRunner runner : runners) {
            runner.stop();
        }
    }
}
