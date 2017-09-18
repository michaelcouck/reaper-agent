package com.pxs.reaper.action;

import com.pxs.reaper.model.Metrics;
import com.pxs.reaper.toolkit.OS;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.*;

@Slf4j
@Setter
@Getter
public class ReaperActionOSMetrics implements ReaperAction, Runnable {

    private volatile Metrics metrics;
    private SigarProxy sigarProxyCache;

    public ReaperActionOSMetrics() {
        Sigar sigar = new Sigar();
        sigarProxyCache = SigarProxyCache.newInstance(sigar, 1000);
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

            metrics.setCpu(cpu);
            metrics.setCpuPerc(cpuPerc);
            metrics.setLoadAverage(loadAverage);
            metrics.setMem(mem);
            metrics.setNetInfo(netInfo);
            metrics.setNetStat(netStat);
            metrics.setProcStat(procStat);
            metrics.setSwap(swap);
            metrics.setTcp(tcp);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            SigarProxyCache.clear(sigarProxyCache);
            // TODO: If the session is closed or disconnected, create it again
        }
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