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
public class ExecutionProperties {
    private MethodsProperties methods = new MethodsProperties();

    @JsonIgnore
    public boolean isEmpty() {
        return methods.isEmpty();
    }

    public void merge(ExecutionProperties other) {
        methods.merge(other.methods);
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

        public void merge(MethodsProperties other) {
            astar.merge(other.astar);
            lm.merge(other.lm);
            core.merge(other.core);
        }


        @Getter
        @Setter
        @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyMapFilter.class)
        public static class AStarProperties {
            private String approximation;
            private Double epsilon;

            @JsonIgnore
            public boolean isEmpty() {
                return approximation == null && epsilon == null;
            }

            public void merge(AStarProperties other) {
                approximation = ofNullable(this.approximation).orElse(other.approximation);
                epsilon = ofNullable(this.epsilon).orElse(other.epsilon);
            }
        }

        @Getter
        @Setter
        @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyMapFilter.class)
        public static class LMProperties {
            @JsonProperty("active_landmarks")
            private Integer activeLandmarks;

            @JsonIgnore
            public boolean isEmpty() {
                return activeLandmarks == null;
            }

            public void merge(LMProperties other) {
                activeLandmarks = ofNullable(this.activeLandmarks).orElse(other.activeLandmarks);
            }
        }

        @Getter
        @Setter
        @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyMapFilter.class)
        public static class CoreProperties {
            @JsonProperty("active_landmarks")
            private Integer activeLandmarks;

            @JsonIgnore
            public boolean isEmpty() {
                return activeLandmarks == null;
            }

            public void merge(CoreProperties other) {
                activeLandmarks = ofNullable(this.activeLandmarks).orElse(other.activeLandmarks);
            }
        }
    }
}

