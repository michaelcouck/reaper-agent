package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.couchbase.core.mapping.Document;

import java.lang.management.ThreadInfo;

/**
 * Contains details for the threads in the Java process.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@Getter
@Setter
@ToString
@Document
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
