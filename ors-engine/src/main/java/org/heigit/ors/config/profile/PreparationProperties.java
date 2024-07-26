package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.config.utils.NonEmptyObjectFilter;

@Getter
@Setter
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

    public PreparationProperties setMethods(MethodsProperties methods) {
        this.methods = methods;
        return this;
    }

    @Getter
    @Setter
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

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CHProperties {
            @Getter(AccessLevel.NONE)
            private Boolean enabled;
            private Integer threads;
            private String weightings;

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

            public Boolean isEnabled() {
                return enabled;
            }

        }
    }
}

