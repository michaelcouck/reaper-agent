package com.pxs.reaper;

import com.pxs.reaper.action.ReaperActionJMXMetrics;
import com.pxs.reaper.action.ReaperActionOSMetrics;
import com.pxs.reaper.toolkit.FILE;
import com.pxs.reaper.toolkit.OS;
import com.pxs.reaper.toolkit.THREAD;
import org.apache.log4j.Logger;
import org.hyperic.sigar.SigarException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;

@SuppressWarnings("unused")
public class Reaper {

    private static final Logger LOGGER = Logger.getLogger(Reaper.class);

    private static final long SLEEP_TIME = 1000;

    private int iterations;
    private final Random random = new Random(31);

    Reaper(final int iterations) throws IOException {
        this.iterations = iterations;
        THREAD.initialize();
        addNativeLibrariesToPath();
        postConstruct();
    }

    @PostConstruct
    public void postConstruct() throws IOException {
        InputStream inputStream = Reaper.class.getResourceAsStream("/reaper.properties");
        Properties properties = new Properties();
        properties.load(inputStream);
    }

    @SuppressWarnings({"unchecked", "InfiniteLoopStatement", "ConstantConditions"})
    void reap() {
        ReaperActionOSMetrics reaperActionOSMetrics = new ReaperActionOSMetrics();
        ReaperActionJMXMetrics reaperActionJMXMetrics = new ReaperActionJMXMetrics();
        Future<Void> future;
        while (true) {
            if (iterations-- == 0) {
                break;
            }
            future = (Future<Void>) THREAD.submit(Reaper.class.getSimpleName(), reaperActionOSMetrics);
            THREAD.waitForFuture(future, SLEEP_TIME);
            future = (Future<Void>) THREAD.submit(Reaper.class.getSimpleName(), reaperActionOSMetrics);
            THREAD.waitForFuture(future, SLEEP_TIME);
            THREAD.sleep(SLEEP_TIME);
            if (!future.isDone()) {
                LOGGER.warn("ReaperAction not finished : " + future.toString());
            }
            // TODO: Check for exceptions and stop actions if too many exceptions...
            // TODO: Retry at longer intervals when high exception count...
        }
    }

    @SuppressWarnings("WeakerAccess")
    void addNativeLibrariesToPath() {
        String javaLibraryPathKey = "java.library.path";
        String javaLibraryPath = System.getProperty(javaLibraryPathKey);
        if (OS.isOs("Linux")) {
            javaLibraryPath = addNativeLibrariesToPath(javaLibraryPath, ".so", ":");
        } else if (OS.isOs("Windows")) {
            javaLibraryPath = addNativeLibrariesToPath(javaLibraryPath, ".dll", ";");
        } else if (OS.isOs("Mac")) {
            javaLibraryPath = addNativeLibrariesToPath(javaLibraryPath, ".dylib", ";");
        } else {
            throw new RuntimeException("Operating system not supported : " + OS.os());
        }
        System.setProperty(javaLibraryPathKey, javaLibraryPath);
        LOGGER.info("Java library path : " + javaLibraryPath);
    }

    @SuppressWarnings("WeakerAccess")
    String addNativeLibrariesToPath(final String javaLibraryPath, final String nativeLibraries, final String separator) {
        List<File> files = FILE.findFilesRecursively(new File("."), new ArrayList<>(), nativeLibraries);
        StringBuilder stringBuilder = new StringBuilder(javaLibraryPath);
        for (final File file : files) {
            stringBuilder.append(separator);
            stringBuilder.append(file.getAbsolutePath());
        }
        return stringBuilder.toString();
    }

    public static void main(final String[] args) throws SigarException, IOException {
        new Reaper(-1).reap();
    }

}