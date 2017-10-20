package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.transport.Transport;
import com.pxs.reaper.transport.WebSocketTransport;
import com.pxs.reaper.model.OSMetrics;
import ikube.toolkit.OS;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.*;
import org.jeasy.props.annotations.Property;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.TimerTask;

import static org.jeasy.props.PropertiesInjectorBuilder.aNewPropertiesInjector;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@Slf4j
@Setter
public class ReaperActionOSMetrics extends TimerTask implements ReaperAction {

    @Property(source = Constant.REAPER_PROPERTIES, key = "sleep-time")
    private int sleepTime;

    private Sigar sigar;
    private Transport transport;
    private SigarProxy sigarProxy;

    public ReaperActionOSMetrics() {
        transport = new WebSocketTransport();
        sigar = new Sigar();
        sigarProxy = SigarProxyCache.newInstance(sigar, 1000);

        aNewPropertiesInjector().injectProperties(this);

        Runtime.getRuntime().addShutdownHook(new Thread(this::terminate));
        Constant.TIMER.scheduleAtFixedRate(this, sleepTime, sleepTime);
    }

    @Override
    public void run() {
        try {
            OSMetrics osMetrics = OSMetrics.builder().build();
            osMetrics.setInetAddress(InetAddress.getLocalHost());

            Cpu[] cpu = cpu(sigarProxy);
            CpuInfo[] cpuInfo = cpuInfo(sigarProxy);
            CpuPerc[] cpuPerc = cpuPerc(sigarProxy);
            Swap swap = swap(sigarProxy);
            double[] loadAverage = loadAverage(sigarProxy);
            Mem mem = mem(sigarProxy);
            NetInfo netInfo = netInfo(sigarProxy);
            NetStat netStat = netStat(sigarProxy);
            ProcStat procStat = procStat(sigarProxy);
            Tcp tcp = tcp(sigarProxy);
            ResourceLimit resourceLimit = resourceLimit(sigarProxy);

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
            SigarProxyCache.clear(sigarProxy);
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
        if (sigar != null) {
            try {
                sigar.close();
            } catch (final Exception e) {
                log.error("Exception closing the Sigar : ", e);
            }
        }
        this.cancel();
        Constant.TIMER.purge();
    }
}