package com.pxs.reaper.agent.action;

import com.pxs.reaper.agent.model.JMetrics;
import com.pxs.reaper.agent.toolkit.THREAD;
import com.pxs.reaper.agent.transport.Transport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(MockitoJUnitRunner.class)
public class ReaperActionJmxMetricsTest {

    @Mock
    private JMetrics jMetrics;
    @Mock
    private Transport transport;
    @Mock
    private MBeanServerConnection mbeanConn;
    @Spy
    @InjectMocks
    private ReaperActionJmxMetrics reaperActionJmxMetrics;

    @Test
    @SuppressWarnings("unchecked")
    public void run() throws IOException, MalformedObjectNameException {
        Mockito.when(mbeanConn.queryNames(null, null)).thenReturn(getObjectNames());
        AtomicReference<Object> objectAtomicReference = new AtomicReference<>();
        Mockito.doAnswer(invocation -> {
            Object[] metrics = invocation.getArguments();
            for (final Object metric : metrics) {
                if (metric != null && JMetrics.class.isAssignableFrom(metric.getClass())) {
                    objectAtomicReference.set(metric);
                }
            }
            return null;
        }).when(transport).postMetrics(Mockito.any(Object.class));
        reaperActionJmxMetrics.run();
        Assert.assertNotNull(objectAtomicReference.get());
    }

    @Test
    public void metricsCollectionsMethods() {
        reaperActionJmxMetrics.misc(jMetrics, ManagementFactory.getRuntimeMXBean());
        reaperActionJmxMetrics.threading(jMetrics, ManagementFactory.getThreadMXBean());
        reaperActionJmxMetrics.memoryPool(jMetrics, ManagementFactory.getMemoryPoolMXBeans());
        reaperActionJmxMetrics.memory(jMetrics, ManagementFactory.getMemoryMXBean());
        reaperActionJmxMetrics.garbageCollection(jMetrics, ManagementFactory.getGarbageCollectorMXBeans());
        reaperActionJmxMetrics.compilation(jMetrics, ManagementFactory.getCompilationMXBean());
        reaperActionJmxMetrics.classloading(jMetrics, ManagementFactory.getClassLoadingMXBean());
        reaperActionJmxMetrics.os(jMetrics, ManagementFactory.getOperatingSystemMXBean());

        Mockito.verify(jMetrics, Mockito.times(1)).setOperatingSystem(Mockito.any());
        Mockito.verify(jMetrics, Mockito.times(1)).setType(JMetrics.class.getName());
        Mockito.verify(jMetrics, Mockito.times(1)).setThreading(Mockito.any());
        Mockito.verify(jMetrics, Mockito.times(1)).setMemoryPools(Mockito.any());
        Mockito.verify(jMetrics, Mockito.times(1)).setMemory(Mockito.any());
        Mockito.verify(jMetrics, Mockito.times(1)).setCompilation(Mockito.any());
        Mockito.verify(jMetrics, Mockito.times(1)).setClassLoading(Mockito.any());
    }

    @Test
    public void terminate() throws IOException {
        THREAD.scheduleAtFixedRate(reaperActionJmxMetrics, Short.MAX_VALUE, Short.MAX_VALUE);
        // The result of the task is dependant on the state and if it has been executed already, so we just run the code
        reaperActionJmxMetrics.terminate();
    }

    private Set<ObjectName> getObjectNames() throws MalformedObjectNameException {
        return new TreeSet<>(Collections.emptyList());
    }

}
