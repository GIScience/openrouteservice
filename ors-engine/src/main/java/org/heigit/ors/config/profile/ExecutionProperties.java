package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.config.utils.NonEmptyObjectFilter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutionProperties {
    private MethodsProperties methods;

    public ExecutionProperties() {
        methods = new MethodsProperties();
    }

    @Getter
    @Setter
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyObjectFilter.class)
    public static class MethodsProperties {
        private AStarProperties astar;
        private LMProperties lm;
        private CoreProperties core;

        public MethodsProperties() {
            astar = new AStarProperties();
            lm = new LMProperties();
            core = new CoreProperties();
        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class AStarProperties {
            private String approximation;
            private Integer epsilon;
        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class LMProperties {
            @JsonProperty("active_landmarks")
            private Integer activeLandmarks;
        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class CoreProperties {
            @JsonProperty("active_landmarks")
            private Integer activeLandmarks;
        }
    }
}

