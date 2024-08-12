package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.config.utils.NonEmptyMapFilter;

@Getter
@Setter
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutionProperties {
    private MethodsProperties methods;

    public ExecutionProperties() {
        methods = new MethodsProperties();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return methods.isEmpty();
    }

    @JsonIgnore
    public void copyProperties(ExecutionProperties execution, boolean overwrite) {
        if (execution == null) {
            return;
        }

        if (this.getMethods() == null) {
            this.setMethods(execution.getMethods());
        } else {
            this.getMethods().copyProperties(execution.getMethods(), overwrite);
        }
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyMapFilter.class)
    public static class MethodsProperties {
        private AStarProperties astar;
        private LMProperties lm;
        private CoreProperties core;

        public MethodsProperties() {
        }

        public MethodsProperties(Boolean setDefaults) {
            if (setDefaults) {
                astar = new AStarProperties();
                lm = new LMProperties();
                core = new CoreProperties();
            }
        }

        @JsonIgnore
        public boolean isEmpty() {
            // check null and empty to catch null pointer exceptions
            return (astar == null || astar.isEmpty()) &&
                    (lm == null || lm.isEmpty()) &&
                    (core == null || core.isEmpty());
        }

        @JsonIgnore
        public void copyProperties(MethodsProperties methods, boolean overwrite) {
            if (methods == null) {
                return;
            }

            if (this.getAstar() == null) {
                this.setAstar(methods.getAstar());
            } else {
                if (methods.getAstar() != null) {
                    this.getAstar().copyProperties(methods.getAstar(), overwrite);
                }
            }

            if (this.getLm() == null) {
                this.setLm(methods.getLm());
            } else {
                if (methods.getLm() != null) {
                    this.getLm().copyProperties(methods.getLm(), overwrite);
                }
            }

            if (this.getCore() == null) {
                this.setCore(methods.getCore());
            } else {
                if (methods.getCore() != null) {
                    this.getCore().copyProperties(methods.getCore(), overwrite);
                }
            }
        }

        @Getter
        @Setter
        @EqualsAndHashCode
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class AStarProperties {
            private String approximation;
            private Double epsilon;

            @JsonIgnore
            public boolean isEmpty() {
                return approximation == null && epsilon == null;
            }

            @JsonIgnore
            public void copyProperties(AStarProperties astar, boolean overwrite) {
                if (astar == null) {
                    return;
                }

                if (this.getApproximation() == null) {
                    this.setApproximation(astar.getApproximation());
                } else {
                    if (astar.getApproximation() != null && overwrite) {
                        this.setApproximation(astar.getApproximation());
                    }
                }

                if (this.getEpsilon() == null) {
                    this.setEpsilon(astar.getEpsilon());
                } else {
                    if (astar.getEpsilon() != null && overwrite) {
                        this.setEpsilon(astar.getEpsilon());
                    }
                }
            }
        }

        @Getter
        @Setter
        @EqualsAndHashCode
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class LMProperties {
            @JsonProperty("active_landmarks")
            private Integer activeLandmarks;

            @JsonIgnore
            public boolean isEmpty() {
                return activeLandmarks == null;
            }

            @JsonIgnore
            public void copyProperties(LMProperties lm, boolean overwrite) {
                if (lm == null) {
                    return;
                }

                if (this.getActiveLandmarks() == null) {
                    this.setActiveLandmarks(lm.getActiveLandmarks());
                } else {
                    if (lm.getActiveLandmarks() != null && overwrite) {
                        this.setActiveLandmarks(lm.getActiveLandmarks());
                    }
                }
            }
        }

        @Getter
        @Setter
        @EqualsAndHashCode
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CoreProperties {
            @JsonProperty("active_landmarks")
            private Integer activeLandmarks;

            @JsonIgnore
            public boolean isEmpty() {
                return activeLandmarks == null;
            }

            @JsonIgnore
            public void copyProperties(CoreProperties core, boolean overwrite) {
                if (core == null) {
                    return;
                }

                if (this.getActiveLandmarks() == null) {
                    this.setActiveLandmarks(core.getActiveLandmarks());
                } else {
                    if (core.getActiveLandmarks() != null && overwrite) {
                        this.setActiveLandmarks(core.getActiveLandmarks());
                    }
                }
            }
        }
    }
}

