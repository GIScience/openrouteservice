/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.api.requests.matrix;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.api.requests.common.RequestOptions;

@ApiModel(value = "Matrix Options", description = "Advanced options for matrix", subTypes = {MatrixRequest.class})
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class MatrixRequestOptions extends RequestOptions {
    public static final String PARAM_DYNAMIC_SPEEDS = "dynamic_speeds";

    @ApiModelProperty(name = PARAM_DYNAMIC_SPEEDS, value = "Option to use dynamic speed updates on some pre-defined speeds.",
            example = "{true}")
    @JsonProperty(PARAM_DYNAMIC_SPEEDS)
    private boolean dynamicSpeeds;
    @JsonIgnore
    private boolean hasDynamicSpeeds = false;


    public boolean getDynamicSpeeds() {
        return dynamicSpeeds;
    }

    public void setDynamicSpeeds(boolean dynamicSpeeds) {
        this.dynamicSpeeds = dynamicSpeeds;
        hasDynamicSpeeds = true;
    }

    public boolean hasDynamicSpeeds() {
        return hasDynamicSpeeds;
    }

}
