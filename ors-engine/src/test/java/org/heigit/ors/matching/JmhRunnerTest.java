package org.heigit.ors.matching;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * JUnit 5 runner for JMH micro-benchmarks.
 *
 * <p>Runs {@link PolygonBBoxBenchmark} in-process (forks=0) so no JVM fork
 * or shaded JAR is needed. Use {@code -Dforks=1} system property to enable
 * forked execution for production-quality measurements.
 *
 * <p>Suppress in regular CI with: {@code -Dgroups=!benchmark} (maven-surefire)
 * or {@code -Dexclude.benchmark.tests=true} if a profile is configured.
 *
 * <p>Run manually:
 * <pre>
 *   ./mvnw test -pl ors-engine -Dtest=JmhRunnerTest
 * </pre>
 */
@Tag("benchmark")
@DisplayName("JMH benchmark runner for PolygonBBox optimizations")
class JmhRunnerTest {

    @Test
    @DisplayName("Given PolygonBBoxBenchmark, when run via JMH Runner, then completes without error")
    void runPolygonBBoxBenchmarks() {
        int forks = Integer.getInteger("forks", 0);  // default: in-process

        Options opts = new OptionsBuilder()
                .include(PolygonBBoxBenchmark.class.getSimpleName())
                .warmupIterations(2)
                .measurementIterations(3)
                .forks(forks)
                .shouldFailOnError(true)
                .resultFormat(ResultFormatType.TEXT)
                .build();

        assertThatNoException()
                .as("JMH benchmark must complete without errors")
                .isThrownBy(() -> new Runner(opts).run());
    }

    @Test
    @DisplayName("Given MatchAreaE2EBenchmark, when run via JMH Runner, then completes without error")
    void runMatchAreaE2EBenchmarks() {
        int forks = Integer.getInteger("forks", 0);

        Options opts = new OptionsBuilder()
                .include(MatchAreaE2EBenchmark.class.getSimpleName())
                .warmupIterations(2)
                .measurementIterations(3)
                .forks(forks)
                .shouldFailOnError(true)
                .resultFormat(ResultFormatType.TEXT)
                .build();

        assertThatNoException()
                .as("MatchAreaE2EBenchmark must complete without errors")
                .isThrownBy(() -> new Runner(opts).run());
    }
}
