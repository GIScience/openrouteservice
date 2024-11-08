package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.config.utils.NonEmptyMapFilter;

import static java.util.Optional.ofNullable;

@Getter
@Setter
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyMapFilter.class)
public class PreparationProperties {
    @JsonProperty("min_network_size")
    private Integer minNetworkSize;
    @JsonProperty("min_one_way_network_size")
    private Integer minOneWayNetworkSize;
    @JsonProperty("methods")
    private MethodsProperties methods = new MethodsProperties();

    public PreparationProperties() {
    }

    public PreparationProperties(String ignored) {
    }

    @JsonIgnore
    public boolean isEmpty() {
        return minNetworkSize == null && minOneWayNetworkSize == null && methods.isEmpty();
    }

    public void merge(PreparationProperties other) {
        minNetworkSize = ofNullable(this.minNetworkSize).orElse(other.minNetworkSize);
        minOneWayNetworkSize = ofNullable(this.minOneWayNetworkSize).orElse(other.minOneWayNetworkSize);
        methods.merge(other.methods);
    }


    @Getter
    @Setter
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyMapFilter.class)
    public static class MethodsProperties {
        private CHProperties ch = new CHProperties();
        private LMProperties lm = new LMProperties();
        private CoreProperties core = new CoreProperties();
        private FastIsochroneProperties fastisochrones = new FastIsochroneProperties();

        @JsonIgnore
        public boolean isEmpty() {
            return (ch == null || ch.isEmpty()) && (lm == null || lm.isEmpty()) && (core == null || core.isEmpty()) && (fastisochrones == null || fastisochrones.isEmpty());
        }

        public void merge(MethodsProperties other) {
            ch.merge(other.ch);
            lm.merge(other.lm);
            core.merge(other.core);
            fastisochrones.merge(other.fastisochrones);
        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CHProperties {
            private Boolean enabled;
            private Integer threads;
            private String weightings;

            @JsonIgnore
            public Integer getThreadsSave() {
                return threads == null || threads < 1 ? 1 : threads;
            }

            @JsonIgnore
            public boolean isEmpty() {
                return enabled == null && threads == null && weightings == null;
            }

            public void merge(CHProperties other) {
                enabled = ofNullable(this.enabled).orElse(other.enabled);
                threads = ofNullable(this.threads).orElse(other.threads);
                weightings = ofNullable(this.weightings).orElse(other.weightings);
            }
        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class LMProperties extends CHProperties {
            private Integer landmarks;

            @JsonIgnore
            public boolean isEmpty() {
                return super.isEmpty() && landmarks == null;
            }

            public void merge(LMProperties other) {
                super.merge(other);
                landmarks = ofNullable(this.landmarks).orElse(other.landmarks);
            }
        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CoreProperties extends LMProperties {
            private String lmsets;

            @JsonIgnore
            public boolean isEmpty() {
                return super.isEmpty() && lmsets == null;
            }

            public void merge(CoreProperties other) {
                super.merge(other);
                lmsets = ofNullable(this.lmsets).orElse(other.lmsets);
            }
        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class FastIsochroneProperties extends CHProperties {
            private Integer maxcellnodes;

            @JsonIgnore
            public boolean isEmpty() {
                return super.isEmpty() && maxcellnodes == null;
            }

            public void merge(FastIsochroneProperties other) {
                super.merge(other);
                maxcellnodes = ofNullable(this.maxcellnodes).orElse(other.maxcellnodes);
            }
        }
    }
}
