package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.utils.NonEmptyMapFilter;

import java.util.Objects;

import static java.util.Optional.ofNullable;

@Getter
@Setter
@EqualsAndHashCode
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
    private MethodsProperties methods = new MethodsProperties();

    public static PreparationProperties getPreparationProperties(EncoderNameEnum encoderName) {
        PreparationProperties preparationProperties = new PreparationProperties();
        switch (encoderName) {
            case DRIVING_CAR -> {
                preparationProperties.setMinNetworkSize(200);
                preparationProperties.getMethods().getCh().setEnabled(true);
                preparationProperties.getMethods().getCh().setThreads(1);
                preparationProperties.getMethods().getCh().setWeightings("fastest");
                preparationProperties.getMethods().getLm().setEnabled(false);
                preparationProperties.getMethods().getLm().setThreads(1);
                preparationProperties.getMethods().getLm().setWeightings("fastest,shortest");
                preparationProperties.getMethods().getLm().setLandmarks(16);
                preparationProperties.getMethods().getCore().setEnabled(true);
                preparationProperties.getMethods().getCore().setThreads(1);
                preparationProperties.getMethods().getCore().setWeightings("fastest,shortest");
                preparationProperties.getMethods().getCore().setLandmarks(64);
                preparationProperties.getMethods().getCore().setLmsets("highways;allow_all");
            }
            case DRIVING_HGV -> {
                preparationProperties.setMinNetworkSize(200);
                preparationProperties.getMethods().getCh().setEnabled(true);
                preparationProperties.getMethods().getCh().setThreads(1);
                preparationProperties.getMethods().getCh().setWeightings("recommended");
                preparationProperties.getMethods().getCore().setEnabled(true);
                preparationProperties.getMethods().getCore().setThreads(1);
                preparationProperties.getMethods().getCore().setWeightings("recommended,shortest");
                preparationProperties.getMethods().getCore().setLandmarks(64);
                preparationProperties.getMethods().getCore().setLmsets("highways;allow_all");
            }
            case DEFAULT -> {
                preparationProperties.setMinNetworkSize(200);
                preparationProperties.getMethods().getLm().setEnabled(true);
                preparationProperties.getMethods().getLm().setThreads(1);
                preparationProperties.getMethods().getLm().setWeightings("recommended,shortest");
                preparationProperties.getMethods().getLm().setLandmarks(16);
            }
            default -> {
            }
        }
        return preparationProperties;

//    TODO: check and apply the changes Julian was going to make to the default values
//    public DefaultPreparationProperties() {
//            super();
//            setMinNetworkSize(200);
//            setMinOneWayNetworkSize(200);
//
//            setMethods(new MethodsProperties(true));
//            getMethods().getCh().setEnabled(false);
//            getMethods().getCh().setWeightings("fastest");
//            getMethods().getCh().setThreads(2);
//
//            getMethods().getLm().setEnabled(true);
//            getMethods().getLm().setThreads(2);
//            getMethods().getLm().setWeightings("recommended,shortest");
//            getMethods().getLm().setLandmarks(16);
//
//            getMethods().getCore().setEnabled(false);
//            getMethods().getCore().setThreads(2);
//            getMethods().getCore().setWeightings("fastest,shortest");
//            getMethods().getCore().setLandmarks(64);
//            getMethods().getCore().setLmsets("highways;allow_all");
//
//            getMethods().getFastisochrones().setEnabled(false);
//            getMethods().getFastisochrones().setThreads(2);
//            getMethods().getFastisochrones().setWeightings("recommended,shortest");
//        }
//
//    public DefaultPreparationProperties(EncoderNameEnum encoderName) {
//            this();
//            if (encoderName == null) {
//                encoderName = EncoderNameEnum.DEFAULT;
//            }
//
//            switch (encoderName) {
//                case DRIVING_CAR -> {
//                    setMinNetworkSize(200);
//                    getMethods().getCh().setEnabled(true);
//                    getMethods().getCh().setWeightings("fastest");
//                    getMethods().getLm().setEnabled(false);
//                    getMethods().getLm().setWeightings("fastest,shortest");
//                    getMethods().getLm().setLandmarks(16);
//                    getMethods().getCore().setEnabled(true);
//                    getMethods().getCore().setWeightings("fastest,shortest");
//                    getMethods().getCore().setLandmarks(64);
//                    getMethods().getCore().setLmsets("highways;allow_all");
//                }
//                case DRIVING_HGV -> {
//                    setMinNetworkSize(200);
//                    getMethods().getCh().setEnabled(true);
//                    getMethods().getCh().setWeightings("recommended");
//                    getMethods().getCore().setEnabled(true);
//                    getMethods().getCore().setWeightings("recommended,shortest");
//                    getMethods().getCore().setLandmarks(64);
//                    getMethods().getCore().setLmsets("highways;allow_all");
//                }
//                default -> {
//                }
//            }
//        }

    }


    @JsonIgnore
    public boolean isEmpty() {
        return minNetworkSize == null && minOneWayNetworkSize == null && (methods == null || methods.isEmpty());
    }

    public void merge(PreparationProperties other, Boolean overwrite) {
        minNetworkSize = overwrite ? ofNullable(other.minNetworkSize).orElse(this.minNetworkSize) : ofNullable(this.minNetworkSize).orElse(other.minNetworkSize);
        minOneWayNetworkSize = overwrite ? ofNullable(other.minOneWayNetworkSize).orElse(this.minOneWayNetworkSize) : ofNullable(this.minOneWayNetworkSize).orElse(other.minOneWayNetworkSize);
        methods.merge(other.methods, overwrite);
    }


    @Getter
    @Setter
    @EqualsAndHashCode
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

        public void merge(MethodsProperties other, Boolean overwrite) {
            ch.merge(other.ch, overwrite);
            lm.merge(other.lm, overwrite);
            core.merge(other.core, overwrite);
            fastisochrones.merge(other.fastisochrones, overwrite);
        }

        @Getter
        @Setter
        @EqualsAndHashCode
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CHProperties {
            private Boolean enabled;
            private Integer threads;
            private String weightings;

            @JsonIgnore
            public boolean isEmpty() {
                return enabled == null && threads == null && weightings == null;
            }

            // Needed for Jackson
            @JsonGetter("enabled")
            private Boolean getEnabled() {
                return enabled;
            }

            @JsonIgnore
            public Boolean isEnabled() {
                return Objects.requireNonNullElse(enabled, false);
            }

            @JsonIgnore
            public Integer getThreadsSave() {
                return threads == null || threads < 1 ? 1 : threads;
            }

            public void merge(CHProperties other, Boolean overwrite) {
                enabled = overwrite ? ofNullable(other.enabled).orElse(this.enabled) : ofNullable(this.enabled).orElse(other.enabled);
                threads = overwrite ? ofNullable(other.threads).orElse(this.threads) : ofNullable(this.threads).orElse(other.threads);
                weightings = overwrite ? ofNullable(other.weightings).orElse(this.weightings) : ofNullable(this.weightings).orElse(other.weightings);
            }
        }

        @Getter
        @Setter
        @EqualsAndHashCode
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

            // Needed for Jackson
            @JsonGetter("enabled")
            private Boolean getEnabled() {
                return enabled;
            }

            @JsonIgnore
            public Boolean isEnabled() {
                return Objects.requireNonNullElse(enabled, false);
            }

            @JsonIgnore
            public Integer getThreadsSave() {
                return threads == null || threads < 1 ? 1 : threads;
            }

            public void merge(LMProperties other, Boolean overwrite) {
                enabled = overwrite ? ofNullable(other.enabled).orElse(this.enabled) : ofNullable(this.enabled).orElse(other.enabled);
                threads = overwrite ? ofNullable(other.threads).orElse(this.threads) : ofNullable(this.threads).orElse(other.threads);
                weightings = overwrite ? ofNullable(other.weightings).orElse(this.weightings) : ofNullable(this.weightings).orElse(other.weightings);
                landmarks = overwrite ? ofNullable(other.landmarks).orElse(this.landmarks) : ofNullable(this.landmarks).orElse(other.landmarks);
            }
        }

        @Getter
        @Setter
        @EqualsAndHashCode
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CoreProperties {
            private Boolean enabled;
            private Integer threads;
            private String weightings;
            private Integer landmarks;
            private String lmsets;

            @JsonIgnore
            public boolean isEmpty() {
                return enabled == null && threads == null && weightings == null && landmarks == null && lmsets == null;
            }

            // Needed for Jackson
            @JsonGetter("enabled")
            private Boolean getEnabled() {
                return enabled;
            }

            @JsonIgnore
            public Boolean isEnabled() {
                return Objects.requireNonNullElse(enabled, false);
            }

            @JsonIgnore
            public Integer getThreadsSave() {
                return threads == null || threads < 1 ? 1 : threads;
            }

            public void merge(CoreProperties other, Boolean overwrite) {
                enabled = overwrite ? ofNullable(other.enabled).orElse(this.enabled) : ofNullable(this.enabled).orElse(other.enabled);
                threads = overwrite ? ofNullable(other.threads).orElse(this.threads) : ofNullable(this.threads).orElse(other.threads);
                weightings = overwrite ? ofNullable(other.weightings).orElse(this.weightings) : ofNullable(this.weightings).orElse(other.weightings);
                landmarks = overwrite ? ofNullable(other.landmarks).orElse(this.landmarks) : ofNullable(this.landmarks).orElse(other.landmarks);
                lmsets = overwrite ? ofNullable(other.lmsets).orElse(this.lmsets) : ofNullable(this.lmsets).orElse(other.lmsets);
            }
        }

        @Getter
        @Setter
        @EqualsAndHashCode
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class FastIsochroneProperties {
            private Boolean enabled;
            private Integer threads;
            private String weightings;
            private Integer maxcellnodes;

            @JsonIgnore
            public boolean isEmpty() {
                return enabled == null && threads == null && weightings == null && maxcellnodes == null;
            }

            // Needed for Jackson
            @JsonGetter("enabled")
            private Boolean getEnabled() {
                return enabled;
            }

            @JsonIgnore
            public Boolean isEnabled() {
                return Objects.requireNonNullElse(enabled, false);
            }

            @JsonIgnore
            public Integer getThreadsSave() {
                return threads == null || threads < 1 ? 1 : threads;
            }

            public void merge(FastIsochroneProperties other, Boolean overwrite) {
                enabled = overwrite ? ofNullable(other.enabled).orElse(this.enabled) : ofNullable(this.enabled).orElse(other.enabled);
                threads = overwrite ? ofNullable(other.threads).orElse(this.threads) : ofNullable(this.threads).orElse(other.threads);
                weightings = overwrite ? ofNullable(other.weightings).orElse(this.weightings) : ofNullable(this.weightings).orElse(other.weightings);
                maxcellnodes = overwrite ? ofNullable(other.maxcellnodes).orElse(this.maxcellnodes) : ofNullable(this.maxcellnodes).orElse(other.maxcellnodes);
            }
        }
    }
}
