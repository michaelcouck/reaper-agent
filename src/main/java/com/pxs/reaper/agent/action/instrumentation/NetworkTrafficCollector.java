package com.pxs.reaper.agent.action.instrumentation;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class NetworkTrafficCollector {

    public NetworkTrafficCollector() {
        System.out.println("NetworkTrafficCollector");
    }

    @SuppressWarnings("unused")
    public static void capture(final Object object) throws IOException, InterruptedException {
        System.out.println("Capturing object : " + object);
    }

}