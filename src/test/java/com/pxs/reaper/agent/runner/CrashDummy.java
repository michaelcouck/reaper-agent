package com.pxs.reaper.agent.runner;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.pxs.reaper.agent.toolkit.THREAD;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CrashDummy {

    @Test
    @Ignore
    public void callFaas() throws UnirestException {
        String faasUri = "http://el5757:8100/rest/fuck-off/Whoever";
        for (int i = 0; i < 10000; i++) {
            HttpResponse<String> response = Unirest.get(faasUri).asString();
            System.out.println(i + ") Response : " + response.getStatus() + ", text : " + response.getBody());
            THREAD.sleep(250);
        }
    }

}