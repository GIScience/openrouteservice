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

import java.util.concurrent.ThreadLocalRandom;

public class ORSKafkaConsumerMessageSpeedUpdate {
    private long edgeId;
    private boolean reverse;
    private int speed;

    public ORSKafkaConsumerMessageSpeedUpdate() {
        this.edgeId = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
        this.reverse = ThreadLocalRandom.current().nextBoolean();
        this.speed = ThreadLocalRandom.current().nextInt(10, 130);
    }

    public ORSKafkaConsumerMessageSpeedUpdate(long edgeId, boolean reverse, int speed) {
        this.edgeId = edgeId;
        this.reverse = reverse;
        this.speed = speed;
    }

    public long getEdgeId() {
        return edgeId;
    }

    public void setEdgeId(long edgeId) {
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
}
