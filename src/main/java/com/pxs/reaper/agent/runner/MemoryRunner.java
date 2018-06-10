package com.pxs.reaper.agent.runner;
/*

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.pxs.reaper.agent.toolkit.THREAD;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;

*/
/**
 * @author Michael Couck
 * @version 01.00
 * @since 02-05-2018
 *//*

@Slf4j
public class MemoryRunner {

    public static void main(final String[] args) throws Exception {
        String url = new URL("http://192.168.1.74:8100/rest/fuck-off/Serge%20gets%20a%20bullet!").toURI().toString();
        for (int i = 0; i < 10000; i++) {
            HttpResponse httpResponse = Unirest.get(url).asString();
            log.info("Response : {}", httpResponse.getBody());
            THREAD.sleep(250);
        }
    }

}*/
