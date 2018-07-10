package org.streampipes.examples.jvm.processor.birch.memoryCalculator;

import java.util.ArrayList;
import java.util.List;

public class Measurer {
    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    public double getMemoryUsageInMB() {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i <= 1000000; i++) {
            list.add(i);
        }
        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
//        System.out.println("Total memory is megabytes: " + bytesToMegabytes(runtime.totalMemory()));
//        System.out.println("Used memory is bytes: " + memory);
//        System.out.println("Used memory is megabytes: " + bytesToMegabytes(memory));

        return bytesToMegabytes(memory);
    }
}
