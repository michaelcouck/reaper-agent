package com.pxs.reaper.agent.toolkit;

import com.pxs.reaper.agent.action.ReaperActionJvmMetrics;
import com.pxs.reaper.agent.model.JMetrics;
import com.pxs.reaper.agent.model.MemoryPool;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * -XX:+UseG1GC -Xms1024m -Xmx1024m
 */
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class MemoryCpuTest {

    @Test
    public void memory() {
        ReaperActionJvmMetrics actionJvmMetrics = new ReaperActionJvmMetrics();
        List<double[]> doubles = new ArrayList<>();
        int iterations = 200;
        do {
            doubles.add(new double[Short.MAX_VALUE * 25]);
            JMetrics metrics = actionJvmMetrics.getMetrics();

            double totalUsed = 0;
            double totalMemory = metrics.getMemory().getTotalMemory();
            for (final MemoryPool memoryPool : metrics.getMemoryPools()) {String name = memoryPool.getName();
                if (name.contains("Metaspace") || name.contains("Code Cache") || name.contains("Compressed Class Space")) {
                    continue;
                }
                totalUsed += memoryPool.getUsage().getUsed();
                System.out.println("Memory pool : " + memoryPool.getName() + ":" + memoryPool.getUsage().getMax());
            }

            double usedPercentage = totalUsed / totalMemory * 100D;
            double freePercantage = 100D - usedPercentage;

            System.out.println(
                    "Total used : " + totalUsed +
                            ", max : " + totalMemory +
                            ", used percentage : " + usedPercentage +
                            ", free percentage : " + freePercantage);
            THREAD.sleep(10);
        } while (iterations-- > 0);
    }

}