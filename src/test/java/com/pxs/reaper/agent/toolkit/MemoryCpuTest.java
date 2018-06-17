package com.pxs.reaper.agent.toolkit;

import com.pxs.reaper.agent.action.ReaperActionJvmMetrics;
import com.pxs.reaper.agent.model.JMetrics;
import com.pxs.reaper.agent.model.Memory;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

public class MemoryCpuTest {

    @Test
    @Ignore
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "InfiniteLoopStatement"})
    public void memory() {
        long megabyte = 1024 * 1024;
        ReaperActionJvmMetrics actionJvmMetrics = new ReaperActionJvmMetrics();
        List<double[]> doubles = new ArrayList<>();
        while (true) {
            doubles.add(new double[Short.MAX_VALUE * 25]);
            JMetrics metrics = actionJvmMetrics.getMetrics();
            Memory memory = metrics.getMemory();
            System.out.println("Free : " + memory.getFreeMemory() / megabyte);
            System.out.println("Max : " + memory.getMaxMemory() / megabyte);
            System.out.println("Total : " + memory.getTotalMemory() / megabyte);

            MemoryUsage heap = memory.getHeapMemoryUsage();
            System.out.println("Heap used : " + heap.getUsed() / megabyte +
                    ", init : " + heap.getInit() / megabyte +
                    ", committed : " + heap.getCommitted() / megabyte +
                    ", max : " + heap.getMax() / megabyte);
            THREAD.sleep(10);
        }
    }

}
