package com.pxs.reaper;

import com.sun.tools.attach.VirtualMachine;
import ikube.toolkit.THREAD;
import lombok.extern.slf4j.Slf4j;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RunWith(JMockit.class)
public class ReaperTest {

    /**
     * Mock the reaper to test the scheduler
     */
    @SuppressWarnings("unused")
    public static class ReaperMock extends MockUp<Reaper> {

        static int attachToOperatingSystemCount = 0;
        static int attachToJavaProcessesCount = 0;

        @Mock
        public void $init() {
            // Do nothing
        }

        @Mock
        void attachToOperatingSystem() {
            attachToOperatingSystemCount++;
        }

        @Mock
        void attachToJavaProcesses() {
            attachToJavaProcessesCount++;
        }
    }

    @Mocked
    private VirtualMachine virtualMachine;

    @Before
    public void before() {
        THREAD.initialize();
    }

    @Test
    public void main() {
        long waitTime = 1000;
        final ReaperMock reaper = new ReaperMock();
        try {
            THREAD.submit("blade-runner", () -> Reaper.main(new String[]{Long.toString(waitTime)}));
            THREAD.sleep(waitTime);
        } finally {
            reaper.tearDown();
        }
        Assert.assertEquals(1, ReaperMock.attachToOperatingSystemCount);
        Assert.assertEquals(1, ReaperMock.attachToJavaProcessesCount);
    }

    @Test
    public void addNativeLibrariesToPath() throws IOException {
        String javaLibraryPath = Reaper.addNativeLibrariesToPath();
        String[] paths = StringUtils.split(javaLibraryPath, File.pathSeparatorChar);
        for (final String path : paths) {
            if (Arrays.deepToString(new File(path).list()).contains(Constant.LINUX_LOAD_MODULE)) {
                return;
            }
        }
        Assert.fail("Should contain the link libraries");
    }

    @Test
    public void attachToOperatingSystem() {
        Reaper reaper = new Reaper();
        reaper.attachToOperatingSystem();
    }

    @Test
    public void detachFromJavaProcesses() {
        final String id = "virtual-machine";
        Reaper reaper = new Reaper();
        new Expectations() {
            {
                virtualMachine.id();
                result = id;
            }
        };
        Map<String, VirtualMachine> virtualMachines = new HashMap<>();
        virtualMachines.put(id, virtualMachine);
        Deencapsulation.setField(reaper, "virtualMachines", virtualMachines);
        reaper.detachFromJavaProcesses();
        Assert.assertEquals(0, virtualMachines.size());
    }

    @Test
    public void attachToJavaProcesses() throws Exception {
        Reaper reaper = new Reaper();
        reaper.attachToJavaProcesses();

        Map<String, VirtualMachine> virtualMachines = Deencapsulation.getField(reaper, "virtualMachines");
        int virtualMachinesSize = virtualMachines.size();
        Assert.assertTrue(virtualMachinesSize > 0);

        reaper.attachToJavaProcesses();
        virtualMachines = Deencapsulation.getField(reaper, "virtualMachines");
        Assert.assertEquals(virtualMachinesSize, virtualMachines.size());
    }

}