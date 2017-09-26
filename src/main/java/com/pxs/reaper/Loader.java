package com.pxs.reaper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import ikube.toolkit.THREAD;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;

public class Loader {

    public static void main(final String[] args) {
        THREAD.initialize();

        for (int i = 0; i < 10; i++) {
            THREAD.submit("", () -> {
                Client client = Client.create();
                WebResource webResource = client.resource("uri");

                WebResource.Builder builder = webResource.getRequestBuilder();
                builder.cookie(new Cookie("", ""));

                MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
                queryParams.add("json", ""); //set parameters for request

                String appKey = "Bearer kmjhsdqfohqdsfmkjh"; // appKey is unique number

                ClientResponse response = null;
                response = webResource.queryParams(queryParams)
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .header("Authorization", appKey)
                        .get(ClientResponse.class);

                String jsonStr = response.getEntity(String.class);
            });
        }
    }

}
