package com.pxs.reaper;

import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import java.io.IOException;

public class ReaperIntegration {

    @Test
    public void reap() throws IOException {
        Reaper reaper = new Reaper();
        Whitebox.setInternalState(reaper, "iterations", 1);
        reaper.reap();
    }

}
