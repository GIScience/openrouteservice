package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.utils.NonEmptyMapFilter;

import static java.util.Optional.ofNullable;

@Getter
@Setter
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutionProperties {
    private MethodsProperties methods = new MethodsProperties();

    public static ExecutionProperties getExecutionProperties(EncoderNameEnum encoderName) {
        ExecutionProperties executionProperties = new ExecutionProperties();
        switch (encoderName) {
            case DRIVING_CAR -> {
                executionProperties.getMethods().getLm().setActiveLandmarks(6);
                executionProperties.getMethods().getCore().setActiveLandmarks(6);
            }
            case DRIVING_HGV -> {
                executionProperties.getMethods().getCore().setActiveLandmarks(6);
            }
            case DEFAULT -> {
                executionProperties.getMethods().getLm().setActiveLandmarks(8);
            }
            default -> {
            }
        }
        return executionProperties;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return methods.isEmpty();
    }

    public void merge(ExecutionProperties other, Boolean overwrite) {
        methods.merge(other.methods, overwrite);
    }

    @Getter
    @Setter
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyMapFilter.class)
    public static class MethodsProperties {
        private AStarProperties astar = new AStarProperties();
        private LMProperties lm = new LMProperties();
        private CoreProperties core = new CoreProperties();

        @JsonIgnore
        public boolean isEmpty() {
            return astar.isEmpty() && lm.isEmpty() && core.isEmpty();
        }

        public void merge(MethodsProperties other, Boolean overwrite) {
            astar.merge(other.astar, overwrite);
            lm.merge(other.lm, overwrite);
            core.merge(other.core, overwrite);
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

            public void merge(AStarProperties other, Boolean overwrite) {
                approximation = overwrite ? ofNullable(other.approximation).orElse(this.approximation) : ofNullable(this.approximation).orElse(other.approximation);
                epsilon = overwrite ? ofNullable(other.epsilon).orElse(this.epsilon) : ofNullable(this.epsilon).orElse(other.epsilon);
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

            public void merge(LMProperties other, Boolean overwrite) {
                activeLandmarks = overwrite ? ofNullable(other.activeLandmarks).orElse(this.activeLandmarks) : ofNullable(this.activeLandmarks).orElse(other.activeLandmarks);
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

            public void merge(CoreProperties other, Boolean overwrite) {
                activeLandmarks = overwrite ? ofNullable(other.activeLandmarks).orElse(this.activeLandmarks) : ofNullable(this.activeLandmarks).orElse(other.activeLandmarks);
            }
        }
    }
}

