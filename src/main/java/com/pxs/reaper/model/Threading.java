package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.management.ThreadInfo;

@Getter
@Setter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Threading {

    private long[] threadIds;
    private long[] deadLockedThreads;
    private long[] monitorDeadLockedThreads;

    private long[] threadCpuTimes;
    private ThreadInfo[] threadInfos;

    private int threadCount;
    private int peakThreadCount;
    private int daemonThreadCount;
    private long totalStartedThreadCount;

}
