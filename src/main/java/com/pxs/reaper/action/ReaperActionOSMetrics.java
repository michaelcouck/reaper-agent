package com.pxs.reaper.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pxs.reaper.Constant;
import com.pxs.reaper.Transport;
import com.pxs.reaper.WebSocketTransport;
import com.pxs.reaper.model.Metrics;
import com.pxs.reaper.toolkit.OS;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.*;
import org.jeasy.props.annotations.Property;

import java.util.TimerTask;

@Slf4j
@Setter
public class ReaperActionOSMetrics extends TimerTask implements ReaperAction {

    @Property(source = Constant.REAPER_PROPERTIES, key = "sleep-time")
    private int sleepTime;

    private Transport transport;
    private SigarProxy sigarProxyCache;

    public ReaperActionOSMetrics() {
        transport = new WebSocketTransport();
        sigarProxyCache = SigarProxyCache.newInstance(new Sigar(), 1000);

        Constant.PROPERTIES_INJECTOR.injectProperties(this);
        Constant.TIMER.scheduleAtFixedRate(this, sleepTime, sleepTime);
    }

    @Override
    public void run() {
        try {
            Cpu cpu = cpu(sigarProxyCache);
            CpuPerc cpuPerc = cpuPerc(sigarProxyCache);
            Swap swap = swap(sigarProxyCache);
            double[] loadAverage = loadAverage(sigarProxyCache);
            Mem mem = mem(sigarProxyCache);
            NetInfo netInfo = netInfo(sigarProxyCache);
            NetStat netStat = netStat(sigarProxyCache);
            ProcStat procStat = procStat(sigarProxyCache);
            Tcp tcp = tcp(sigarProxyCache);

            Metrics metrics = Metrics.builder().build();
            metrics.setCpu(cpu);
            metrics.setCpuPerc(cpuPerc);
            metrics.setLoadAverage(loadAverage);
            metrics.setMem(mem);
            metrics.setNetInfo(netInfo);
            metrics.setNetStat(netStat);
            metrics.setProcStat(procStat);
            metrics.setSwap(swap);
            metrics.setTcp(tcp);

            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson GSON = gsonBuilder.create();
            String postage = GSON.toJson(metrics);
            log.error(postage);

            transport.postMetrics(metrics);
        } catch (final SigarException e) {
            throw new RuntimeException(e);
        } finally {
            SigarProxyCache.clear(sigarProxyCache);
            // TODO: If the session is closed or disconnected, create it again
        }
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

    @Override
    public void terminate() {
        this.cancel();
        Constant.TIMER.purge();
    }
}