package com.pxs.reaper.action;

import com.pxs.reaper.model.Metrics;
import com.pxs.reaper.toolkit.OS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hyperic.sigar.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@Slf4j
@ClientEndpoint
public class ReaperActionOSMetrics implements ReaperAction, Runnable {

    private static long COUNTER = 0;

    private static SigarProxy SIGAR_PROXY_CACHE;

    private final Session session;

    public ReaperActionOSMetrics(final int sleepTime, final String reaperWebSocketUri) {
        Sigar sigar = new Sigar();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = URI.create(reaperWebSocketUri);
        try {
            session = container.connectToServer(this, uri);
        } catch (final DeploymentException | IOException e) {
            throw new RuntimeException("Error connecting to : " + uri, e);
        }
        SIGAR_PROXY_CACHE = SigarProxyCache.newInstance(sigar, sleepTime);
    }

    @Override
    public void run() {
        try {
            Cpu cpu = cpu(SIGAR_PROXY_CACHE);
            CpuPerc cpuPerc = cpuPerc(SIGAR_PROXY_CACHE);
            Swap swap = swap(SIGAR_PROXY_CACHE);
            double[] loadAverage = loadAverage(SIGAR_PROXY_CACHE);
            Mem mem = mem(SIGAR_PROXY_CACHE);
            NetInfo netInfo = netInfo(SIGAR_PROXY_CACHE);
            NetStat netStat = netStat(SIGAR_PROXY_CACHE);
            ProcStat procStat = procStat(SIGAR_PROXY_CACHE);
            Tcp tcp = tcp(SIGAR_PROXY_CACHE);

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

            RemoteEndpoint.Async async = session.getAsyncRemote();
            async.sendText(GSON.toJson(metrics));
            if (COUNTER++ % 1000 == 0) {
                log.info("Metrics sent : " + ToStringBuilder.reflectionToString(metrics));
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            SigarProxyCache.clear(SIGAR_PROXY_CACHE);
            // TODO: If the session is closed or disconnected, create it again
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
        onClose(session);
    }

    @SuppressWarnings("unused")
    private Who[] who(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getWhoList();
    }

    @SuppressWarnings("unused")
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
        if (OS.isOs("Linux")) {
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

}