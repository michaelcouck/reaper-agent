package com.pxs.reaper;

import be.bgc.erm.mii.service.state.status.AccountStatus;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import ikube.toolkit.THREAD;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class Loader {

    public static void main(final String[] args) {
        THREAD.initialize();

        /*final String authUser = "id851622";
        final String authPassword = "Caherl2ne";
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );
        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);*/

        THREAD.submit("fls-login", () -> {
        });

        // String appKey = "Bearer kmjhsdqfohqdsfmkjh";
        // .header("Authorization", appKey)

        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("username", "bartvv");
        queryParams.add("password", "instanet01");

        // ?username=bartvv&password=instanet01

        // https://sphinx.skynet.be/rest/billing-account-overview-aggregator
        // https://sphinx.skynet.be/mii/identify/explicit/fls
        Client client = Client.create();
        WebResource webResource = client
                .resource("https://sphinx.skynet.be/mii/identify/explicit/fls?username=bartvv&password=instanet01"); // ?username=bartvv&password=instanet01
        /*String response = webResource
                .queryParams(queryParams)
                .entity("{}")
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON) // application/json;charset=UTF-8
                .post(String.class);*/
        // String jsonStr = response.getEntity(String.class);

        Object secondResponse =webResource
                // .type("application/x-www-form-urlencoded")
                // .queryParams(queryParams)
                .accept(MediaType.APPLICATION_JSON)
                // .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, queryParams);
        System.out.println(secondResponse);
        // System.out.println(secondResponse.getEntity(String.class));
        // System.out.println(response);
    }

}