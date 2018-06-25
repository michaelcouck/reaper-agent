package com.pxs.reaper.agent.runner;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.pxs.reaper.agent.toolkit.THREAD;
import org.junit.Ignore;
import org.junit.Test;

public class CrashTestDummy {

    @Test
    @Ignore
    public void callFaas() throws UnirestException {
        String faasUri = "http://192.168.1.74:8100/rest/fuck-off/Whoever";
        double sleep = 250 * 1.5;
        for (int i = 0; i < 1000000; i++) {
            HttpResponse<String> response = Unirest.get(faasUri).asString();
            if (i % 100 == 0) {
                System.out.println(i + ") Response : " + response.getStatus() + ", text : " + response.getBody() + ", sleep : " + sleep);
                sleep -= 1;
            }
            if (response.getStatus() != 200) {
                System.out.println(i + ") Failed : " + response.getStatus() + ", text : " + response.getBody() + ", sleep : " + sleep);
                sleep *= 1.5D;
            }
            THREAD.sleep((long) Math.abs(sleep));
        }
    }

}