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

package heigit.ors.globalResponseProcessor.gpx.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * {@link WptTypeExtensions} represents a class to process the Extensions for {@link WptType}
 * Can be manually extended if needed
 * @author Julian Psotta, julian@openrouteservice.org
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "distance",
        "duration",
        "type",
        "step"
        // always add new variables here! and below
})

public class WptTypeExtensions extends ExtensionsType {
    protected double distance;
    protected double duration;
    protected int type;
    protected int step;

    /**
     * Gets the value of the distance property
     *
     * @return distance as double
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Sets the value of the distance property
     *
     * @param distance needs a double as input
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Gets the value of the duration property
     *
     * @return duration as double
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property
     *
     * @param duration needs a double as input
     */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * Gets the value of the type property
     *
     * @return type as int
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the value of the type property
     *
     * @param type needs an int as input
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Gets the value of the step property
     *
     * @return step as int
     */
    public int getStep() {
        return step;
    }

    /**
     * Sets the value of the step property
     *
     * @param step needs an int as input
     */
    public void setStep(int step) {
        this.step = step;
    }
}
