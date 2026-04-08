package org.heigit.ors.matching;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * JMH benchmark runner for MatchAreaE2EBenchmark.
 * 
 * This class provides a convenient way to run the benchmark and capture results
 * without requiring the full JMH Maven plugin setup.
 */
public class MatchAreaE2EBenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MatchAreaE2EBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(3)
                .warmupTime(org.openjdk.jmh.runner.options.TimeValue.seconds(1))
                .measurementIterations(5)
                .measurementTime(org.openjdk.jmh.runner.options.TimeValue.seconds(1))
                .build();

        new Runner(opt).run();
    }
}
