package com.pxs.reaper;

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
        THREAD.submit("fls-login", () -> {
        });

        new LoginToProxy().login();

        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("username", "bartvv");
        queryParams.add("password", "instanet01");

        Client client = Client.create();
        WebResource webResource = client.resource("https://sphinx.skynet.be/mii/identify/explicit/fls");

        Object secondResponse = webResource
                .queryParams(queryParams)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(ClientResponse.class);
        System.out.println(secondResponse);
    }

}

class LoginToProxy {
    void login() {
        final String authUser = "id851622";
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
        System.setProperty("http.proxyPassword", authPassword);

        // https://sphinx.skynet.be/rest/billing-account-overview-aggregator
        // https://sphinx.skynet.be/mii/identify/explicit/fls
        // String appKey = "Bearer kmjhsdqfohqdsfmkjh";
        // .header("Authorization", appKey)
    }
}