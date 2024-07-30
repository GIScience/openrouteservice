package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.heigit.ors.config.utils.NonEmptyMapFilter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreparationProperties {
    @Setter
    @JsonProperty("min_network_size")
    private Integer minNetworkSize;
    @Setter
    @JsonProperty("min_one_way_network_size")
    private Integer minOneWayNetworkSize;
    @JsonProperty("methods")
    @Accessors(chain = true)
    private MethodsProperties methods;

    public PreparationProperties() {
        this.methods = new MethodsProperties();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return this.minNetworkSize == null && this.minOneWayNetworkSize == null && methods.isEmpty();
    }

    @Getter
    @Setter
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyMapFilter.class)
    public static class MethodsProperties {
        private CHProperties ch;
        private LMProperties lm;
        private CoreProperties core;
        private FastIsochroneProperties fastisochrones;

        public MethodsProperties() {
        }


        public MethodsProperties(Boolean setDefaults) {
            if (setDefaults) {
                this.ch = new CHProperties();
                this.lm = new LMProperties();
                this.core = new CoreProperties();
                this.fastisochrones = new FastIsochroneProperties();
            }
        }

        @JsonIgnore
        public boolean isEmpty() {
            return ch.isEmpty() && lm.isEmpty() && core.isEmpty() && fastisochrones.isEmpty();
        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CHProperties {
            @Getter(AccessLevel.NONE)
            private Boolean enabled;
            private Integer threads;
            private String weightings;

            @JsonIgnore
            public boolean isEmpty() {
                return enabled == null && threads == null && weightings == null;
            }

            @JsonIgnore
            public Boolean isEnabled() {
                return enabled;
            }

        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class LMProperties {
            @Getter(AccessLevel.NONE)
            private Boolean enabled;
            private Integer threads;
            private String weightings;
            private Integer landmarks;

            @JsonIgnore
            public boolean isEmpty() {
                return enabled == null && threads == null && weightings == null && landmarks == null;
            }

            @JsonIgnore
            public Boolean isEnabled() {
                return enabled;
            }

        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CoreProperties {
            @Getter(AccessLevel.NONE)
            private Boolean enabled;
            private Integer threads;
            private String weightings;
            private Integer landmarks;
            private String lmsets;

            @JsonIgnore
            public boolean isEmpty() {
                return enabled == null && threads == null && weightings == null && landmarks == null && lmsets == null;
            }

            @JsonIgnore
            public Boolean isEnabled() {
                return enabled;
            }

        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class FastIsochroneProperties {
            @Getter(AccessLevel.NONE)
            private Boolean enabled;
            private Integer threads;
            private String weightings;
            private Integer maxcellnodes;

            @JsonIgnore
            public boolean isEmpty() {
                return enabled == null && threads == null && weightings == null && maxcellnodes == null;
            }

            @JsonIgnore
            public Boolean isEnabled() {
                return enabled;
            }
        }
    }
}

