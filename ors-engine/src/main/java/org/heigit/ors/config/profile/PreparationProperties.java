package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.heigit.ors.config.utils.NonEmptyMapFilter;

import java.util.Objects;

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
    private MethodsProperties methods;

    public PreparationProperties() {
        this.methods = new MethodsProperties();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return minNetworkSize == null && minOneWayNetworkSize == null && (methods == null || methods.isEmpty());
    }

    @JsonIgnore
    public void copyProperties(PreparationProperties preparation, boolean overwrite) {
        if (preparation == null) {
            return;
        }

        if (this.getMinNetworkSize() == null) {
            setMinNetworkSize(preparation.getMinNetworkSize());
        } else {
            if (preparation.getMinNetworkSize() != null && overwrite) {
                setMinNetworkSize(preparation.getMinNetworkSize());
            }
        }

        if (this.getMinOneWayNetworkSize() == null) {
            setMinOneWayNetworkSize(preparation.getMinOneWayNetworkSize());
        } else {
            if (preparation.getMinOneWayNetworkSize() != null && overwrite) {
                setMinOneWayNetworkSize(preparation.getMinOneWayNetworkSize());
            }
        }

        if (this.getMethods() == null) {
            setMethods(preparation.getMethods());
        } else {
            if (preparation.getMethods() != null) {
                getMethods().copyProperties(preparation.getMethods(), overwrite);
            }
        }
    }

    @Getter
    @Setter
    @EqualsAndHashCode
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
            return (ch == null || ch.isEmpty()) && (lm == null || lm.isEmpty()) && (core == null || core.isEmpty()) && (fastisochrones == null || fastisochrones.isEmpty());
        }

        public void copyProperties(MethodsProperties methods, boolean overwrite) {
            if (methods == null) {
                return;
            }

            if (this.getCh() == null) {
                setCh(methods.getCh());
            } else {
                if (methods.getCh() != null) {
                    getCh().copyProperties(methods.getCh(), overwrite);
                }
            }

            if (this.getLm() == null) {
                setLm(methods.getLm());
            } else {
                if (methods.getLm() != null) {
                    getLm().copyProperties(methods.getLm(), overwrite);
                }
            }

            if (this.getCore() == null) {
                setCore(methods.getCore());
            } else {
                if (methods.getCore() != null) {
                    getCore().copyProperties(methods.getCore(), overwrite);
                }
            }

            if (this.getFastisochrones() == null) {
                setFastisochrones(methods.getFastisochrones());
            } else {
                if (methods.getFastisochrones() != null) {
                    getFastisochrones().copyProperties(methods.getFastisochrones(), overwrite);
                }
            }
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

            public void copyProperties(CHProperties ch, boolean overwrite) {
                if (ch == null) {
                    return;
                }

                if (this.getEnabled() == null) {
                    setEnabled(ch.getEnabled());
                } else {
                    if (ch.getEnabled() != null && overwrite) {
                        setEnabled(ch.getEnabled());
                    }
                }

                if (this.getThreads() == null) {
                    setThreads(ch.getThreads());
                } else {
                    if (ch.getThreads() != null && overwrite) {
                        setThreads(ch.getThreads());
                    }
                }

                if (this.getWeightings() == null) {
                    setWeightings(ch.getWeightings());
                } else {
                    if (ch.getWeightings() != null && overwrite) {
                        setWeightings(ch.getWeightings());
                    }
                }
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

            public void copyProperties(LMProperties lm, boolean overwrite) {
                if (lm == null) {
                    return;
                }

                if (this.getEnabled() == null) {
                    setEnabled(lm.getEnabled());
                } else {
                    if (lm.getEnabled() != null && overwrite) {
                        setEnabled(lm.getEnabled());
                    }
                }

                if (this.getThreads() == null) {
                    setThreads(lm.getThreads());
                } else {
                    if (lm.getThreads() != null && overwrite) {
                        setThreads(lm.getThreads());
                    }
                }

                if (this.getWeightings() == null) {
                    setWeightings(lm.getWeightings());
                } else {
                    if (lm.getWeightings() != null && overwrite) {
                        setWeightings(lm.getWeightings());
                    }
                }

                if (this.getLandmarks() == null) {
                    setLandmarks(lm.getLandmarks());
                } else {
                    if (lm.getLandmarks() != null && overwrite) {
                        setLandmarks(lm.getLandmarks());
                    }
                }
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

            public void copyProperties(CoreProperties core, boolean overwrite) {
                if (core == null) {
                    return;
                }

                if (this.getEnabled() == null) {
                    setEnabled(core.getEnabled());
                } else {
                    if (core.getEnabled() != null && overwrite) {
                        setEnabled(core.getEnabled());
                    }
                }

                if (this.getThreads() == null) {
                    setThreads(core.getThreads());
                } else {
                    if (core.getThreads() != null && overwrite) {
                        setThreads(core.getThreads());
                    }
                }

                if (this.getWeightings() == null) {
                    setWeightings(core.getWeightings());
                } else {
                    if (core.getWeightings() != null && overwrite) {
                        setWeightings(core.getWeightings());
                    }
                }

                if (this.getLandmarks() == null) {
                    setLandmarks(core.getLandmarks());
                } else {
                    if (core.getLandmarks() != null && overwrite) {
                        setLandmarks(core.getLandmarks());
                    }
                }

                if (this.getLmsets() == null) {
                    setLmsets(core.getLmsets());
                } else {
                    if (core.getLmsets() != null && overwrite) {
                        setLmsets(core.getLmsets());
                    }
                }
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

            public void copyProperties(FastIsochroneProperties fastisochrones, boolean overwrite) {
                if (fastisochrones == null) {
                    return;
                }

                if (this.getEnabled() == null) {
                    setEnabled(fastisochrones.getEnabled());
                } else {
                    if (fastisochrones.getEnabled() != null && overwrite) {
                        setEnabled(fastisochrones.getEnabled());
                    }
                }

                if (this.getThreads() == null) {
                    setThreads(fastisochrones.getThreads());
                } else {
                    if (fastisochrones.getThreads() != null && overwrite) {
                        setThreads(fastisochrones.getThreads());
                    }
                }

                if (this.getWeightings() == null) {
                    setWeightings(fastisochrones.getWeightings());
                } else {
                    if (fastisochrones.getWeightings() != null && overwrite) {
                        setWeightings(fastisochrones.getWeightings());
                    }
                }

                if (this.getMaxcellnodes() == null) {
                    setMaxcellnodes(fastisochrones.getMaxcellnodes());
                } else {
                    if (fastisochrones.getMaxcellnodes() != null && overwrite) {
                        setMaxcellnodes(fastisochrones.getMaxcellnodes());
                    }
                }
            }
        }
    }
}
