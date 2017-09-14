package com.pxs.reaper;

import org.junit.Test;

public class ReaperIntegration {

    @Test
    public void reap() {
        Reaper reaper = new Reaper(10);
        reaper.reap();
    }

}
