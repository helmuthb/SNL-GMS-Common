package flatfile.tester;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import data.Constants;
import data.WaveformGenerators;

/**
 * Created by jwvicke on 11/16/17.
 */
public class FlatFileReadWriteTester {

    private static final String OUTPUT_FOLDER = "/tmp/";

    public static void runWriteTests() throws Exception {
        for (int size : Constants.TEST_SIZES) {
            double[] data = WaveformGenerators.buildSamples(size);
            File outputFile = getFile(size);
            // Delete output file if it exists already (to avoid appending)
            if (outputFile.exists()) {
                if (!outputFile.delete()) {
                    throw new Exception("Could not delete test output file " + outputFile.getAbsolutePath());
                }
                outputFile = getFile(size);
            }
            testWrite(data, outputFile);
        }
    }

    public static void runReadTests() throws Exception {
        for (int size : Constants.TEST_SIZES) {
            testRead(size, getFile(size));
        }
    }

    private static void testWrite(double[] data, File outputFile) throws IOException {
        long start = System.currentTimeMillis();
        DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(outputFile));
        for(double x : data) {
            outputStream.writeDouble(x);
        }
        outputStream.flush();
        long end = System.currentTimeMillis();
        long totalMillis = end - start;
        float millisPerPoint = (float) totalMillis / (float) data.length;
        String msg = String.format("Flat File write test: Wrote %d points in %d millis (%f millis per point).",
                                    data.length, totalMillis, millisPerPoint);
        System.out.println(msg);
    }

    private static void testRead(int size, File outputFile) throws Exception {
        long start = System.currentTimeMillis();
        DataInputStream inputStream = new DataInputStream(new FileInputStream(outputFile));
        double[] readData = new double[size];
        int index = 0;
        while(inputStream.available() >= Double.BYTES) {
            readData[index++] = inputStream.readDouble();
        }
        if (index != size) {
            throw new Exception("Expected to find " + size + " points in file, but only found " + (index));
        }
        long end = System.currentTimeMillis();
        long totalMillis = end - start;
        float millisPerPoint = (float) totalMillis / (float) size;
        String msg = String.format("Flat File read test: Read %d points in %d millis (%f millis per point).",
                size, totalMillis, millisPerPoint);
        System.out.println(msg);

    }

    private static File getFile(int numPoints) {
        String path = OUTPUT_FOLDER + "flat-file-test-" + numPoints + "-points";
        return new File(path);
    }
}
