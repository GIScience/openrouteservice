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

    @Getter
    @Setter
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
            return astar.isEmpty() && lm.isEmpty() && core.isEmpty();
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
        }
    }
}

