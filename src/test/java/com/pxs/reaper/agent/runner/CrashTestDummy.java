package com.pxs.reaper.agent.runner;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.pxs.reaper.agent.toolkit.THREAD;
import org.junit.Test;

public class CrashTestDummy {

    @Test
    public void callFaas() throws UnirestException {
        String faasUri = "http://192.168.1.74:8100/rest/fuck-off/Whoever";
        for (int i = 0; i < 1000000; i++) {
            HttpResponse<String> response = Unirest.get(faasUri).asString();
            if (i % 100 == 0) {
                System.out.println(i + ") Response : " + response.getStatus() + ", text : " + response.getBody());
            }
            if (response.getStatus() != 200) {
                System.out.println(i + ") Failed : " + response.getStatus() + ", text : " + response.getBody());
                break;
            }
            THREAD.sleep(250);
        }
    }

}