package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.heigit.ors.config.utils.NonEmptyObjectFilter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreparationProperties {
    @JsonProperty("min_network_size")
    private Integer minNetworkSize;
    @JsonProperty("min_one_way_network_size")
    private Integer minOneWayNetworkSize;
    @JsonProperty("methods")
    private MethodsProperties methods;

    public PreparationProperties() {
        this.methods = new MethodsProperties();
    }

    public Integer getMinNetworkSize() {
        return minNetworkSize;
    }

    public void setMinNetworkSize(Integer minNetworkSize) {
        this.minNetworkSize = minNetworkSize;
    }

    public Integer getMinOneWayNetworkSize() {
        return minOneWayNetworkSize;
    }

    public void setMinOneWayNetworkSize(Integer minOneWayNetworkSize) {
        this.minOneWayNetworkSize = minOneWayNetworkSize;
    }

    public MethodsProperties getMethods() {
        return methods;
    }

    public PreparationProperties setMethods(MethodsProperties methods) {
        this.methods = methods;
        return this;
    }

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyObjectFilter.class)
    public static class MethodsProperties {
        private CHProperties ch;
        private LMProperties lm;
        private CoreProperties core;
        private FastIsochroneProperties fastisochrones;

        public MethodsProperties() {
            ch = new CHProperties();
            lm = new LMProperties();
            core = new CoreProperties();
            fastisochrones = new FastIsochroneProperties();
        }

        public CHProperties getCh() {
            return ch;
        }

        public void setCh(CHProperties ch) {
            this.ch = ch;
        }

        public LMProperties getLm() {
            return lm;
        }

        public void setLm(LMProperties lm) {
            this.lm = lm;
        }

        public CoreProperties getCore() {
            return core;
        }

        public void setCore(CoreProperties core) {
            this.core = core;
        }

        public FastIsochroneProperties getFastisochrones() {
            return fastisochrones;
        }

        public void setFastisochrones(FastIsochroneProperties fastisochrones) {
            this.fastisochrones = fastisochrones;
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CHProperties {
            private Boolean enabled;
            private Integer threads;
            private String weightings;

            public Boolean getEnabled() {
                return enabled;
            }

            public void setEnabled(Boolean enabled) {
                this.enabled = enabled;
            }

            public Integer getThreads() {
                return threads;
            }

            public void setThreads(Integer threads) {
                this.threads = threads;
            }

            public String getWeightings() {
                return weightings;
            }

            public void setWeightings(String weightings) {
                this.weightings = weightings;
            }
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class LMProperties {
            private Boolean enabled;
            private Integer threads;
            private String weightings;
            private Integer landmarks;

            public Boolean getEnabled() {
                return enabled;
            }

            public void setEnabled(Boolean enabled) {
                this.enabled = enabled;
            }

            public Integer getThreads() {
                return threads;
            }

            public void setThreads(Integer threads) {
                this.threads = threads;
            }

            public String getWeightings() {
                return weightings;
            }

            public void setWeightings(String weightings) {
                this.weightings = weightings;
            }

            public Integer getLandmarks() {
                return landmarks;
            }

            public void setLandmarks(Integer landmarks) {
                this.landmarks = landmarks;
            }
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CoreProperties {
            private Boolean enabled;
            private Integer threads;
            private String weightings;
            private Integer landmarks;
            private String lmsets;

            public Boolean getEnabled() {
                return enabled;
            }

            public void setEnabled(Boolean enabled) {
                this.enabled = enabled;
            }

            public Integer getThreads() {
                return threads;
            }

            public void setThreads(Integer threads) {
                this.threads = threads;
            }

            public String getWeightings() {
                return weightings;
            }

            public void setWeightings(String weightings) {
                this.weightings = weightings;
            }

            public Integer getLandmarks() {
                return landmarks;
            }

            public void setLandmarks(Integer landmarks) {
                this.landmarks = landmarks;
            }

            public String getLmsets() {
                return lmsets;
            }

            public void setLmsets(String lmsets) {
                this.lmsets = lmsets;
            }
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class FastIsochroneProperties {
            private Boolean enabled;
            private Integer threads;
            private String weightings;
            private Integer maxcellnodes;

            public Boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(Boolean enabled) {
                this.enabled = enabled;
            }

            public Integer getThreads() {
                return threads;
            }

            public void setThreads(Integer threads) {
                this.threads = threads;
            }

            public String getWeightings() {
                return weightings;
            }

            public void setWeightings(String weightings) {
                this.weightings = weightings;
            }

            public Integer getMaxcellnodes() {
                return maxcellnodes;
            }

            public void setMaxcellnodes(Integer maxcellnodes) {
                this.maxcellnodes = maxcellnodes;
            }
        }
    }
}

