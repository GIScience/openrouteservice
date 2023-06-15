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

package org.heigit.ors.api.responses.routing.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.heigit.ors.routing.RouteWarning;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Informs about possible difficulties like access restrictions on the generated route. Generates a corresponding `extras` object with the affected segments.")
public class JSONWarning {
    @ApiModelProperty(value = "Identification code for the warning", example = "1")
    @JsonProperty
    private final Integer code;

    @ApiModelProperty( value = "The message associated with the warning", example = "This route may go over restricted roads")
    @JsonProperty
    private final String message;

    public JSONWarning(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public JSONWarning(RouteWarning warning) {
        this.code = warning.getWarningCode();
        this.message = warning.getWarningMessage();
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
