package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class ExecutionProperties {
    private MethodsProperties methods;

    public MethodsProperties getMethods() {
        return methods;
    }

    public void setMethods(MethodsProperties methods) {
        this.methods = methods;
    }

    @JsonInclude(NON_NULL)
    public static class MethodsProperties {
        private AStarProperties astar;
        private LMProperties lm;
        private CoreProperties core;

        public AStarProperties getCh() {
            return astar;
        }

        public void setCh(AStarProperties ch) {
            this.astar = ch;
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

        @JsonInclude(NON_NULL)
        public static class AStarProperties {
            private String approximation;
            private Integer epsilon;

            public String getApproximation() {
                return approximation;
            }

            public void setApproximation(String approximation) {
                this.approximation = approximation;
            }

            public Integer getEpsilon() {
                return epsilon;
            }

            public void setEpsilon(Integer epsilon) {
                this.epsilon = epsilon;
            }
        }

        @JsonInclude(NON_NULL)
        public static class LMProperties {
            @JsonProperty("active_landmarks")
            private Integer activeLandmarks;

            public Integer getActiveLandmarks() {
                return activeLandmarks;
            }

            public void setActiveLandmarks(Integer activeLandmarks) {
                this.activeLandmarks = activeLandmarks;
            }
        }

        @JsonInclude(NON_NULL)
        public static class CoreProperties {
            @JsonProperty("active_landmarks")
            private Integer activeLandmarks;

            public Integer getActiveLandmarks() {
                return activeLandmarks;
            }

            public void setActiveLandmarks(Integer activeLandmarks) {
                this.activeLandmarks = activeLandmarks;
            }
        }
    }
}