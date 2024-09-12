package org.sitenv.spring.configuration;

import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

@Component("keyCloackTokenValidationClient")
public class KeyCloackTokenValidationClient {

    /** The LOGGERGER */
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyCloackTokenValidationClient.class);
    public static final String TOKEN = "token";
    private static final String APPLICATION_URL_FORM_ENCODED = "application/x-www-form-urlencoded";

    public boolean validateToken(HttpServletRequest request) {

        Properties prop = fetchProperties();
        String authUrl = System.getProperty("keycloak.auth.server") == null ?  prop.getProperty("keycloak.auth.server") : System.getProperty("keycloak.auth.server");
        String realm = System.getProperty("keycloak.realm") == null ?  prop.getProperty("keycloak.realm") : System.getProperty("keycloak.realm");
        String clientId = System.getProperty("keycloak.client.id") == null ?  prop.getProperty("keycloak.client.id") : System.getProperty("keycloak.client.id");
        String clientSecret = System.getProperty("keycloak.client.secret") == null ?  prop.getProperty("keycloak.client.secret") : System.getProperty("keycloak.client.secret");


        LOGGER.info("Entry - validateToken Method in KeyCloackTokenValidationClient ");
        boolean validationResponse = false;
        final String authorizationHeaderValue = request.getHeader("Authorization");
        if (authorizationHeaderValue != null && authorizationHeaderValue.startsWith("Bearer"));
            String token = authorizationHeaderValue.substring(7, authorizationHeaderValue.length());

        String url = authUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";

        OkHttpClient client = new OkHttpClient().newBuilder().hostnameVerifier(new HostnameVerifier()
        {
            @Override
            public boolean verify(String hostname, SSLSession session)
            {
                return true;
            }
        }).build();

        MediaType mediaType = MediaType.parse(APPLICATION_URL_FORM_ENCODED);

        RequestBody body = RequestBody.create(mediaType, "token="+token+"&client_id="+clientId+"&client_secret="+clientSecret);

        Request requestOne = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response clientResponse;
        try {
            clientResponse = client.newCall(requestOne).execute();
            clientResponse.body();
            if (!clientResponse.isSuccessful()) {
                LOGGER.error("Failed to authenticate");
                //throw new RuntimeException("Failed to authenticate");
            }
            String response = clientResponse.body().string();
            JSONObject jsonObj = new JSONObject(response);
            validationResponse = (boolean) jsonObj.get("active");
            LOGGER.info("Access Token Validation Status ::::" + validationResponse);
        } catch (IOException e) {
            LOGGER.info("Exception - validateToken Method in KeyCloackTokenValidationClient",e);
            e.printStackTrace();
        }
        LOGGER.info("Exit - validateToken Method in KeyCloackTokenValidationClient ");
        return validationResponse;

    }

    public static Properties fetchProperties(){
        Properties properties = new Properties();
        try {
            File file = ResourceUtils.getFile("classpath:application.properties");
            InputStream in = new FileInputStream(file);
            properties.load(in);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return properties;
    }

}
