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
package org.heigit.ors.fastisochrones.partitioning;

/**
 * Lightweight entry object for ordering nodes in a queue/deque based on weight.
 *
 * @author Hendrik Leuschner
 */
public class EKEdgeEntry implements Comparable<EKEdgeEntry> {
    private final int node;
    private final int weight;

    public EKEdgeEntry(int node, int weight) {
        this.node = node;
        this.weight = weight;
    }

    public int getNode() {
        return node;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public int compareTo(EKEdgeEntry o) {
        if (node == o.node && weight == o.weight)
            return 0;

        if (weight < o.weight)
            return -1;
        if (weight > o.weight)
            return 1;
        //Same weight case
        return node < o.node ? -1 : 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EKEdgeEntry other = (EKEdgeEntry) obj;
        return (node == other.node && weight == other.weight);
    }

    @Override
    public int hashCode() {
        return node * 31 + weight;
    }
}
