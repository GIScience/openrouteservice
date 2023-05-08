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

import java.security.SecureRandom;

public class ORSKafkaConsumerMessageSpeedUpdate {
    private int edgeId;
    private boolean reverse;
    private int speed;
    private int durationMin;

    public static ORSKafkaConsumerMessageSpeedUpdate generateRandom() {
        ORSKafkaConsumerMessageSpeedUpdate msg = new ORSKafkaConsumerMessageSpeedUpdate();
        SecureRandom random = new SecureRandom();
        msg.setEdgeId(random.nextInt(100));
        msg.setReverse(random.nextBoolean());
        msg.setSpeed(random.nextInt(130));
        msg.setDurationMin(random.nextInt(120));
        return msg;
    }

    public int getEdgeId() {
        return edgeId;
    }

    public void setEdgeId(int edgeId) {
        this.edgeId = edgeId;
    }

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDurationMin() {
        return durationMin;
    }

    public boolean hasDurationMin() {
        return this.durationMin > 0;
    }

    public void setDurationMin(int durationMin) {
        this.durationMin = durationMin;
    }
}
