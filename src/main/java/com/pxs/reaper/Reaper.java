package com.pxs.reaper;

import com.google.gson.Gson;
import com.pxs.reaper.model.Metrics;
import com.pxs.reaper.toolkit.OS;
import com.pxs.reaper.toolkit.THREAD;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.hyperic.sigar.*;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.Future;

@ClientEndpoint
@SuppressWarnings("unused")
public class Reaper {

    private static final Logger LOGGER = Logger.getLogger(Reaper.class);

    private static final long SLEEP_TIME = 1000;

    private int iterations;
    private final Random random = new Random(31);
    private final URI uri = URI.create("http://metrics-reaper:8888/web-socket");

    public Reaper(final int iterations) {
        this.iterations = iterations;
        THREAD.initialize();
    }

    void test() {
        for (int i = 0; i < iterations; i++) {
            THREAD.sleep(SLEEP_TIME);
        }
    }

    @SuppressWarnings({"unchecked", "InfiniteLoopStatement"})
    void reap() {
        final Sigar sigar = new Sigar();
        final SigarProxy sigarProxy = SigarProxyCache.newInstance(sigar, (int) SLEEP_TIME);
        while (true) {
            if (iterations-- == 0) {
                break;
            }
            Future<Void> future = reap(sigarProxy);
            THREAD.waitForFuture(future, SLEEP_TIME);
            SigarProxyCache.clear(sigarProxy);
            THREAD.sleep(SLEEP_TIME);
            assert future != null;
            if (!future.isDone()) {
                LOGGER.warn("Action not finished : " + future.toString());
            }
            // TODO: Check for exceptions and stop actions if too many exceptions...
            // TODO: Retry at longer intervals when high exception count...
        }
    }

    @SuppressWarnings("unchecked")
    private Future<Void> reap(final SigarProxy sigarProxy) {
        return (Future<Void>) THREAD.submit(Reaper.class.getSimpleName(), new Runnable() {
            public void run() {
                try {
                    Cpu cpu = cpu(sigarProxy);
                    CpuPerc cpuPerc = cpuPerc(sigarProxy);
                    Swap swap = swap(sigarProxy);
                    double[] loadAverage = loadAverage(sigarProxy);
                    Mem mem = mem(sigarProxy);
                    NetInfo netInfo = netInfo(sigarProxy);
                    NetStat netStat = netStat(sigarProxy);
                    ProcStat procStat = procStat(sigarProxy);
                    Tcp tcp = tcp(sigarProxy);
                    Uptime uptime = upTime(sigarProxy);
                    Who[] whos = who(sigarProxy);

                    Metrics metrics = Metrics.builder()
                            .cpu(cpu)
                            .cpuPerc(cpuPerc)
                            .loadAverage(loadAverage)
                            .mem(mem)
                            .netInfo(netInfo)
                            .netStat(netStat)
                            .procStat(procStat)
                            .swap(swap)
                            .tcp(tcp)
                            .build();

                    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                    Session session = container.connectToServer(this, uri);
                    Gson gson = new Gson();
                    session.getAsyncRemote().sendText(gson.toJson(metrics));
                    if (random.nextInt(10000) / 1000 == 0) {
                        LOGGER.info(ToStringBuilder.reflectionToString(metrics));
                    }
                } catch (final Exception e) {
                    LOGGER.error("Exception in reaper : ", e);
                    throw new RuntimeException(e);
                } finally {
                    THREAD.destroy(Reaper.class.getSimpleName());
                }
            }
        });
    }

    private Who[] who(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getWhoList();
    }

    private Uptime upTime(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getUptime();
    }

    private Tcp tcp(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getTcp();
    }

    private ProcStat procStat(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getProcStat();
    }

    private NetStat netStat(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getNetStat();
    }

    private NetInfo netInfo(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getNetInfo();
    }

    private Mem mem(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getMem();
    }

    private double[] loadAverage(final SigarProxy sigarProxy) throws SigarException {
        if (OS.isOs("linux")) {
            return sigarProxy.getLoadAverage();
        }
        return new double[0];
    }

    private CpuPerc cpuPerc(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getCpuPerc();
    }

    private Cpu cpu(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getCpu();
    }

    private Swap swap(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getSwap();
    }

    public static void main(final String[] args) throws SigarException {
        new Reaper(-1).reap();
    }

}