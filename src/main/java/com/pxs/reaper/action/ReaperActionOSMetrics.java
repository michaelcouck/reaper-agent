package com.pxs.reaper.action;

import com.google.gson.Gson;
import com.pxs.reaper.Reaper;
import com.pxs.reaper.model.Metrics;
import com.pxs.reaper.toolkit.OS;
import com.pxs.reaper.toolkit.THREAD;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hyperic.sigar.*;

import javax.websocket.*;
import java.net.URI;

@ClientEndpoint
public class ReaperActionOSMetrics extends ReaperAAction implements Runnable {

    private static final long SLEEP_TIME = 1000;

    private static SigarProxy SIGAR_PROXY_CACHE;

    private final URI uri = URI.create("ws://reaper-microservice-reaper.b9ad.pro-us-east-1.openshiftapps.com/reaper-websocket");

    public ReaperActionOSMetrics() {
        setupSigar();
    }

    private void setupSigar() {
        Sigar sigar = new Sigar();
        SIGAR_PROXY_CACHE = SigarProxyCache.newInstance(sigar, (int) SLEEP_TIME);
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
            Uptime uptime = upTime(SIGAR_PROXY_CACHE);
            Who[] whos = who(SIGAR_PROXY_CACHE);

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

            Gson gson = new Gson();
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            Session session = container.connectToServer(this, uri);
            RemoteEndpoint.Async async = session.getAsyncRemote();
            async.sendText(gson.toJson(metrics));
            logger.info(ToStringBuilder.reflectionToString(metrics));
        } catch (final Exception e) {
            logger.error("Exception in reaper : ", e);
            throw new RuntimeException(e);
        } finally {
            SigarProxyCache.clear(SIGAR_PROXY_CACHE);
            THREAD.destroy(Reaper.class.getSimpleName());
        }
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
