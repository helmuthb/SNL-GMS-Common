package data;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Utility class with functions to generate different shapes of waveform data.
 * Created by jwvicke on 11/13/17.
 */
public class WaveformGenerators {

    public static Waveform buildWaveform(int size) {
        return new Waveform(Instant.EPOCH, 40.0, size, buildSamples(size));
    }

    public static double[] buildSamples(int size) {
        double[] points = new double[size];
        for (int i = 0; i < size; i++) {
            points[i] = i % 500;
        }
        return points;
    }

    /**
     * Creates waveform data of a constant value (flat).
     * @param constantValue the constant value to assign to each point of the waveform
     * @param startTime the start time of the waveform data
     * @param size the number of samples
     * @param sampleRate the number of samples per second, e.g. 40 for 40HZ
     * @return a list of pairs of (Instant, Double), which represent timed waveform samples.
     */
    public static List<ImmutablePair<Instant, Double>> flat(
            double constantValue, Instant startTime, int size, double sampleRate) {

        double[] samples = new double[size];
        Arrays.fill(samples, constantValue);
        return getTimedPoints(samples, startTime, size, sampleRate);
    }

    /**
     * Creates random waveform data.
     * @param startTime the start time of the waveform data
     * @param size the number of samples
     * @param sampleRate the number of samples per second, e.g. 40 for 40HZ
     * @return a list of pairs of (Instant, Double), which represent timed waveform samples.
     */
    public static List<ImmutablePair<Instant, Double>> random(
            Instant startTime, int size, double sampleRate) {

        Random rand = new Random();
        double[] samples = new double[size];
        for (int i = 0; i < size; i++) {
            samples[i] = rand.nextDouble();
        }
        return getTimedPoints(samples, startTime, size, sampleRate);
    }

    /**
     * Creates waveform data that steps (at rate of changePerPoint) up from min
     * to max and then back down to min (fluctuates smoothly in [min,max]).
     * @param min the minimum value
     * @param max the maximum value
     * @param changePerPoint the step size (slope) to increase/decrease each point by from the previous value.
     * @param startTime the start time of the waveform data
     * @param size the number of samples
     * @param sampleRate the number of samples per second, e.g. 40 for 40HZ
     * @return a list of pairs of (Instant, Double), which represent timed waveform samples.
     */
    public static List<ImmutablePair<Instant, Double>> continuouslyBetween(
            double min, double max, double changePerPoint, Instant startTime, int size, double sampleRate) {

        boolean adding = true;
        double[] samples = new double[size];
        double val = min;
        for (int i = 0; i < size; i++) {
            samples[i] = val;
            if (val >= max) {
                adding = false;
            }
            else if (val <= min) {
                adding = true;
            }
            val += adding? changePerPoint : -changePerPoint;

        }
        return getTimedPoints(samples, startTime, size, sampleRate);
    }

    /**
     * Gets a list of timed point pairs given the samples, start time, size, and sample rate.
     * Uses the built-in function of COI object Waveform to do this.  This function is a
     * helper for the other public ones of this class.
     * @param samples the samples of the waveform
     * @param startTime the start time of the waveform data
     * @param size the number of samples
     * @param sampleRate the number of samples per second, e.g. 40 for 40HZ
     * @return a list of pairs of (Instant, Double), which represent timed waveform samples.
     */
    private static List<ImmutablePair<Instant, Double>> getTimedPoints(
            double[] samples, Instant startTime, int size, double sampleRate) {

        Waveform wf = new Waveform(startTime, sampleRate, size, samples);
        return wf.asTimedPairs();
    }

    public static void main(String[] args) {
        List<ImmutablePair<Instant, Double>> flatPoints
                = flat(0.25, Instant.now(), 50, 50);
        List<ImmutablePair<Instant, Double>> randomPoints
                = random(Instant.now(), 50, 2);
        List<ImmutablePair<Instant, Double>> continousPoints
                = continuouslyBetween(-5.5, 10.5, 0.25, Instant.now(), 100, 2);
    }
}
