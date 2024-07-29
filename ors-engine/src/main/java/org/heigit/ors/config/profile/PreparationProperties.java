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
    @JsonProperty("min_network_size")
    private Integer minNetworkSize;
    @JsonProperty("min_one_way_network_size")
    private Integer minOneWayNetworkSize;
    @JsonProperty("methods")
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
            ch = new CHProperties();
            lm = new LMProperties();
            core = new CoreProperties();
            fastisochrones = new FastIsochroneProperties();
        }

        @JsonIgnore
        public boolean isEmpty() {
            return ch.isEmpty() && lm.isEmpty() && core.isEmpty() && fastisochrones.isEmpty();
        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CHProperties {
            private Boolean enabled;
            private Integer threads;
            private String weightings;

            @JsonIgnore
            public boolean isEmpty() {
                return enabled == null && threads == null && weightings == null;
            }
        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class LMProperties {
            private Boolean enabled;
            private Integer threads;
            private String weightings;
            private Integer landmarks;

            @JsonIgnore
            public boolean isEmpty() {
                return enabled == null && threads == null && weightings == null && landmarks == null;
            }
        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CoreProperties {
            private Boolean enabled;
            private String threads;
            private String weightings;
            private Integer landmarks;
            private String lmsets;

            @JsonIgnore
            public boolean isEmpty() {
                return enabled == null && threads == null && weightings == null && landmarks == null && lmsets == null;
            }
        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class FastIsochroneProperties {
            @Setter(AccessLevel.NONE)
            private Boolean enabled;
            private Integer threads;
            private String weightings;
            private Integer maxcellnodes;

            @JsonIgnore
            public boolean isEmpty() {
                return enabled == null && threads == null && weightings == null && maxcellnodes == null;
            }
        }
    }
}

