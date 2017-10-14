package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.Transport;
import com.pxs.reaper.WebSocketTransport;
import com.pxs.reaper.model.OSMetrics;
import com.pxs.reaper.toolkit.OS;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.*;
import org.jeasy.props.annotations.Property;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.TimerTask;

import static org.jeasy.props.PropertiesInjectorBuilder.aNewPropertiesInjector;

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

        aNewPropertiesInjector().injectProperties(this);
        Constant.TIMER.scheduleAtFixedRate(this, sleepTime, sleepTime);
    }

    @Override
    public void run() {
        try {
            OSMetrics osMetrics = OSMetrics.builder().build();
            osMetrics.setInetAddress(InetAddress.getLocalHost());

            Cpu[] cpu = cpu(sigarProxyCache);
            CpuInfo[] cpuInfo = cpuInfo(sigarProxyCache);
            CpuPerc[] cpuPerc = cpuPerc(sigarProxyCache);
            Swap swap = swap(sigarProxyCache);
            double[] loadAverage = loadAverage(sigarProxyCache);
            Mem mem = mem(sigarProxyCache);
            NetInfo netInfo = netInfo(sigarProxyCache);
            NetStat netStat = netStat(sigarProxyCache);
            ProcStat procStat = procStat(sigarProxyCache);
            Tcp tcp = tcp(sigarProxyCache);
            ResourceLimit resourceLimit = resourceLimit(sigarProxyCache);

            osMetrics.setCpu(cpu);
            osMetrics.setCpuPerc(cpuPerc);
            osMetrics.setCpuInfo(cpuInfo);

            osMetrics.setLoadAverage(loadAverage);
            osMetrics.setMem(mem);
            osMetrics.setNetInfo(netInfo);
            osMetrics.setNetStat(netStat);
            osMetrics.setProcStat(procStat);
            osMetrics.setSwap(swap);
            osMetrics.setTcp(tcp);
            osMetrics.setResourceLimit(resourceLimit);

            transport.postMetrics(osMetrics);
        } catch (final SigarException | UnknownHostException e) {
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

    private CpuPerc[] cpuPerc(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getCpuPercList();
    }

    private Cpu[] cpu(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getCpuList();
    }

    private CpuInfo[] cpuInfo(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getCpuInfoList();
    }

    private Swap swap(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getSwap();
    }

    private ResourceLimit resourceLimit(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getResourceLimit();
    }

    @Override
    public void terminate() {
        this.cancel();
        Constant.TIMER.purge();
    }
}