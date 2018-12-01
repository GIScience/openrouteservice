/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   	http://www.giscience.uni-hd.de
 *   	http://www.heigit.org
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
package heigit.ors.v2.services.config;

import java.util.List;
import java.util.Map;

public class StatisticsProviderConfiguration {
    private int _id;
    private String _name;
    private Map<String, Object> _parameters;
    private Map<String, String> _mapping;
    private String _attribution;

    public StatisticsProviderConfiguration(int id, String name, Map<String, Object> parameters, Map<String, String> mapping, String attribution) {
        _id = id;
        _name = name;
        _parameters = parameters;
        _mapping = mapping;
        _attribution = attribution;
    }

    @Override
    public int hashCode() {
        return _id;
    }

    @Override
    public boolean equals(Object obj) {
        return _id == ((StatisticsProviderConfiguration) obj)._id;
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public String getAttribution() {
        return _attribution;
    }

    public Map<String, Object> getParameters() {
        return _parameters;
    }

    public String[] getMappedProperties(List<String> props) {
        String[] res = new String[props.size()];

        for (int i = 0; i < props.size(); i++) {
            res[i] = _mapping.get(props.get(i));
        }

        return res;
    }
}
