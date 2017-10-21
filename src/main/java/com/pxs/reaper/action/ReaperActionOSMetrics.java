package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.OSMetrics;
import com.pxs.reaper.transport.Transport;
import com.pxs.reaper.transport.WebSocketTransport;
import ikube.toolkit.OS;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.TimerTask;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@Slf4j
public class ReaperActionOSMetrics extends TimerTask implements ReaperAction {

    private Sigar sigar;
    private Transport transport;
    private SigarProxy sigarProxy;

    public ReaperActionOSMetrics() {
        sigar = new Sigar();
        transport = new WebSocketTransport();
        sigarProxy = SigarProxyCache.newInstance(sigar, 1000);
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
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public boolean terminate() {
        synchronized (sigar) {
            try {
                sigar.close();
            } catch (final Exception e) {
                log.error("Exception closing the Sigar : ", e);
            }
        }
        boolean terminated = cancel();
        Constant.TIMER.purge();
        return terminated;
    }
}