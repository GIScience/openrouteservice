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
package heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.weighting.ConsistentWeightApproximator;
import com.graphhopper.routing.weighting.WeightApproximator;

/**
 * Augmentation of ConsistentWeightApproximator to set proxy weights.
 *
 * @author A Oles, H Leuschner
 *
 */
public class ConsistentCoreWeightApproximator extends ConsistentWeightApproximator {

    public ConsistentCoreWeightApproximator(WeightApproximator weightApprox) {
        super(weightApprox);
    }

    public void setFromWeight(double weight){
        if(getApproximation() instanceof CoreLMApproximator)
            ((CoreLMApproximator)getReverseApproximation()).setProxyWeight(weight);
    }
    public void setToWeight(double weight){
        if(getApproximation() instanceof CoreLMApproximator)
            ((CoreLMApproximator)getApproximation()).setProxyWeight(weight);
    }

    public void setVirtEdgeWeightFrom(double weight){
        if(getApproximation() instanceof CoreLMApproximator)
            ((CoreLMApproximator)getReverseApproximation()).setVirtEdgeWeight(weight);
    }
    public void setVirtEdgeWeightTo(double weight){
        if(getApproximation() instanceof CoreLMApproximator)
            ((CoreLMApproximator)getApproximation()).setVirtEdgeWeight(weight);
    }

}
