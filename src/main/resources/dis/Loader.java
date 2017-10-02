package dis;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import ikube.toolkit.THREAD;

import javax.net.ssl.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

public class Loader {

    public static void main(final String[] args) throws KeyManagementException, NoSuchAlgorithmException {
        new LoginToProxy().login();
        new SslCertificateTruster().trust();

        THREAD.initialize();

        final int users = 1;
        final int requests = 1;

        for (int i = 0; i < users; i++) {
            THREAD.submit("load", () -> {
                for (int j = 0; j < requests; j++) {
                    callMii();
                    // myNumber();
                }
                THREAD.destroy("load");
                System.exit(0);
            });
        }
    }

    static void callMii() {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("username", "bartvv"); // bartvv
        queryParams.add("password", "instanet01"); // instanet01
        queryParams.add("msisdn", "???"); // ???

        Client client = Client.create();
        client.setFollowRedirects(Boolean.FALSE);
        WebResource webResource = client.resource("https://sphinx.skynet.be/mii/identify/explicit/fls");

        ClientResponse response = webResource
                .queryParams(queryParams)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);

        // System.out.println("Authentication : " + response.getEntity(String.class));

        callUsage(response);
    }

    static void callUsage(final ClientResponse response) {
        Client client = Client.create();
        client.setFollowRedirects(Boolean.FALSE);
        WebResource webResource = client.resource("https://sphinx.skynet.be/historical-usage-prepaid/prepaid/history/usage/1234567890");

        WebResource.Builder builder = webResource.getRequestBuilder();

        setCookiesHeadersProperties(response, builder);

        ClientResponse response2 = builder
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);
        System.out.println("Usage : " + response2.getEntity(String.class));
    }

    static WebResource setCookiesHeadersProperties(final ClientResponse response, final WebResource.Builder builder) {
        for (final Map.Entry<String, List<String>> header : response.getHeaders().entrySet()) {
            System.out.println("Header : " + header);
            builder.header(header.getKey(), header.getValue());
        }
        for (final Map.Entry<String, Object> property : response.getProperties().entrySet()) {
            System.out.println("Property : " + property);
            // builder.setProperty(property.getKey(), property.getValue());
        }
        for (final NewCookie newCookie : response.getCookies()) {
            System.out.println("Cookie : " + newCookie);
            builder.cookie(newCookie);
            if (newCookie.getName().contains("JSESSIONID")) {
                // builder.header("Set-Cookie", newCookie);
            }
        }
        return null;
    }

}

class SslCertificateTruster {

    void trust() throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = (hostname, session) -> true;

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
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
        System.setProperty("https.proxyUser", authUser);
        System.setProperty("https.proxyPassword", authPassword);

        /*System.setProperty("javax.net.ssl.trustStore", "C:/Data/cert-factory/my-cacerts");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");*/

        /*System.setProperty("http.proxyHost", "userproxy.glb.ebc.local");
        System.setProperty("http.proxyPort", "8080");*/

        // https://sphinx.skynet.be/rest/billing-account-overview-aggregator
        // https://sphinx.skynet.be/mii/identify/explicit/fls
        // String appKey = "Bearer kmjhsdqfohqdsfmkjh";
        // .header("Authorization", appKey)
    }
}