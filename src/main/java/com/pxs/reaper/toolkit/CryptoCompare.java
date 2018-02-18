package com.pxs.reaper.toolkit;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;

public class CryptoCompare {

    public static void main(final String[] arbs) throws UnirestException {
        new CryptoCompare().matrix();
    }

    private void matrix() throws UnirestException {
        String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=BTC,ETH&tsyms=EUR&e=Coinbase&extraParams=your_app_name";
        GetRequest getRequest = Unirest.get(url);
        HttpResponse<String> httpResponse = getRequest.asString();
        String body = httpResponse.getBody();
        System.out.println(body);
    }

}
