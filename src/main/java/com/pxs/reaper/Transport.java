package com.pxs.reaper;

import com.pxs.reaper.model.Metrics;

@SuppressWarnings("WeakerAccess")
public interface Transport {

    void postMetrics(final Metrics metrics);

}
