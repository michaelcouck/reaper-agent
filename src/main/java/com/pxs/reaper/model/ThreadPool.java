package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThreadPool {

    private long blockedCount;
    private long blockedTime;
    private long lockedStackDepth;
    private long waitedCount;
    private long waitedTime;
    private long inNative;
    private long suspended;

    private List<Enum<Thread.State>> threadStates = new ArrayList<>();

}
