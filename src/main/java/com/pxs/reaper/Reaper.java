package com.pxs.reaper;

import com.pxs.reaper.action.ReaperActionJMXMetrics;
import com.pxs.reaper.action.ReaperActionOSMetrics;
import com.pxs.reaper.toolkit.FILE;
import com.pxs.reaper.toolkit.THREAD;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.SigarException;
import org.jeasy.props.annotations.Property;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

import static org.jeasy.props.PropertiesInjectorBuilder.aNewPropertiesInjector;

@Slf4j
@Setter
@Getter
@SuppressWarnings("unused")
public class Reaper {

    public static final String REAPER_PROPERTIES = "reaper.properties";

    @Property(source = REAPER_PROPERTIES, key = "sleep-time")
    private int sleepTime;
    @Property(source = REAPER_PROPERTIES, key = "iterations")
    private int iterations;
    @Property(source = REAPER_PROPERTIES, key = "reaper-web-socket-uri")
    private String reaperWebSocketUri;

    public Reaper() throws IOException {
        aNewPropertiesInjector().injectProperties(this);
        THREAD.initialize();
        addNativeLibrariesToPath();
    }

    void reap() {
        ReaperActionOSMetrics reaperActionOSMetrics = new ReaperActionOSMetrics(sleepTime, reaperWebSocketUri);
        ReaperActionJMXMetrics reaperActionJMXMetrics = new ReaperActionJMXMetrics(reaperWebSocketUri);
        Future<?> future;
        while (true) {
            if (iterations-- == 0) {
                break;
            }
            future = THREAD.submit(ReaperActionOSMetrics.class.getSimpleName(), reaperActionOSMetrics);
            THREAD.waitForFuture(future, sleepTime);
            future = THREAD.submit(ReaperActionJMXMetrics.class.getSimpleName(), reaperActionJMXMetrics);
            THREAD.waitForFuture(future, sleepTime);
            THREAD.sleep(sleepTime);
            assert future != null;
            if (!future.isDone()) {
                log.warn("ReaperAction not finished : " + future.toString());
            }
            // TODO: Check for exceptions and stop actions if too many exceptions...
            // TODO: Retry at longer intervals when high exception count...
        }
    }

    /**
     * Adds the native libraries folder to the path and returns the library folder path. Also appends
     * the native library path to the system property {@code Constant.javaLibraryPathKey}.
     *
     * @return the path to the native libraries for all the operating systems
     */
    @SuppressWarnings("WeakerAccess")
    String addNativeLibrariesToPath() {
        String javaLibraryPath = System.getProperty(Constant.javaLibraryPathKey);

        StringBuilder stringBuilder = new StringBuilder(javaLibraryPath);
        File linuxLoadModule = FILE.findFileRecursively(new File("."), "libsigar-amd64-linux.so");
        if (linuxLoadModule == null) {
            throw new RuntimeException("Native libraries not found, please put 'lib' folder relative to agent start directory");
        }
        File libDirectory = linuxLoadModule.getParentFile();
        stringBuilder.append(File.pathSeparator);
        stringBuilder.append(FILE.cleanFilePath(libDirectory.getAbsolutePath()));
        stringBuilder.append(File.pathSeparator);

        System.setProperty(Constant.javaLibraryPathKey, stringBuilder.toString());

        return libDirectory.getAbsolutePath();
    }

    public static void main(final String[] args) throws SigarException, IOException {
        new Reaper().reap();
    }

}