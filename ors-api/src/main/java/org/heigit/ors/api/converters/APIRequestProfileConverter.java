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

package org.heigit.ors.api.converters;

import org.heigit.ors.api.requests.common.APIEnums;
import org.springframework.core.convert.converter.Converter;

public class APIRequestProfileConverter implements Converter<String, APIEnums.Profile> {
    @Override
    public APIEnums.Profile convert(String s) {
        for (APIEnums.Profile profile : APIEnums.Profile.values()) {
            if (profile.toString().equals(s)) {
                return profile;
            }
        }
        // This is a workaround for the breaking change: https://github.com/spring-projects/spring-framework/issues/26679
        // This is fixed with a "missingAfterConversion" flag in spring-boot 3.x.x.
        // Required parameters raised a HttpMessageConversionException before spring 2.7.x. when present but wrong.
        // With spring boot 2.7.x it raises a MissingServletRequestParameterException.
        // By throwing a RuntimeException in the custom converter spring itself now raises a HttpMessageConversionException.
        throw new IllegalArgumentException("unsupported profile");
    }
}
