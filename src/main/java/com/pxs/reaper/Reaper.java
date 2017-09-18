package com.pxs.reaper;

import com.google.gson.Gson;
import com.pxs.reaper.action.ReaperActionJMXMetrics;
import com.pxs.reaper.action.ReaperActionOSMetrics;
import com.pxs.reaper.model.Metrics;
import com.pxs.reaper.toolkit.FILE;
import com.pxs.reaper.toolkit.THREAD;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hyperic.sigar.SigarException;
import org.jeasy.props.annotations.Property;

import javax.websocket.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.jeasy.props.PropertiesInjectorBuilder.aNewPropertiesInjector;

@Slf4j
@Setter
@Getter
public class Reaper {

    public static final String REAPER_PROPERTIES = "reaper.properties";

    private static long COUNTER = 0;

    @Property(source = REAPER_PROPERTIES, key = "sleep-time")
    private int sleepTime;
    @Property(source = REAPER_PROPERTIES, key = "iterations")
    private int iterations;
    @Property(source = REAPER_PROPERTIES, key = "reaper-web-socket-uri")
    private String reaperWebSocketUri;

    private Session session;

    Reaper() throws IOException {
        aNewPropertiesInjector().injectProperties(this);
        THREAD.initialize();
        addNativeLibrariesToPath();
    }

    void reap() {
        ReaperActionOSMetrics reaperActionOSMetrics = new ReaperActionOSMetrics();
        ReaperActionJMXMetrics reaperActionJMXMetrics = new ReaperActionJMXMetrics();
        while (true) {
            Metrics metrics = Metrics.builder().build();
            reaperActionOSMetrics.setMetrics(metrics);
            reaperActionJMXMetrics.setMetrics(metrics);

            THREAD.submit(ReaperActionOSMetrics.class.getSimpleName(), reaperActionOSMetrics);
            THREAD.submit(ReaperActionJMXMetrics.class.getSimpleName(), reaperActionJMXMetrics);
            THREAD.sleep(sleepTime);

            postMetrics(metrics);

            if (iterations-- == 0) {
                break;
            }
            if (COUNTER++ % 1000 == 0) {
                log.info("Metrics sent : " + ToStringBuilder.reflectionToString(metrics));
            }
            // TODO: Check for exceptions and stop actions if too many exceptions...
            // TODO: Retry at longer intervals when high exception count...
        }
    }

    private void postMetrics(final Metrics metrics) {
        getTransport();
        Gson GSON = new Gson();
        RemoteEndpoint.Async async = session.getAsyncRemote();
        async.sendText(GSON.toJson(metrics));
    }

    private void getTransport() {
        if (session == null || !session.isOpen()) {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = URI.create(reaperWebSocketUri);
            try {
                session = container.connectToServer(this, uri);
            } catch (final DeploymentException | IOException e) {
                // TODO: Kep re-trying, service could be down
                throw new RuntimeException("Error connecting to : " + uri, e);
            }
        }
    }

    @OnOpen
    public void onOpen(final Session session) throws IOException {
        log.debug("Session opened : " + session.getId());
    }

    @OnMessage
    @SuppressWarnings("UnusedParameters")
    public void onMessage(final String message, final Session session) throws IOException {
        log.info("Got message : " + message);
    }

    @OnClose
    public void onClose(final Session session) {
        log.debug("Session closed : " + session.getId());
    }

    @OnError
    public void onError(final Session session, final Throwable throwable) {
        log.error("Error in session : " + session.getId(), throwable);
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