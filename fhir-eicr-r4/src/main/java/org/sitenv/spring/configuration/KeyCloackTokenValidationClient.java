package org.sitenv.spring.configuration;

import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

@Component("keyCloackTokenValidationClient")
public class KeyCloackTokenValidationClient {

    /**
     * The LOGGERGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyCloackTokenValidationClient.class);
    public static final String TOKEN = "token";
    private static final String APPLICATION_URL_FORM_ENCODED = "application/x-www-form-urlencoded";

    @Autowired
    private Environment environment;



    private static final List<String> requiredProperties = List.of(
            "keycloak.auth.server",
            "keycloak.realm",
            "keycloak.client.id",
            "keycloak.client.secret"
    );

    private boolean isConfigValidated = false;

    private String authUrl;
    private String realm;
    private String clientId;
    private String clientSecret;



    public void validateConfig() {
        // Perform the validation once during initialization
        List<String> missingConfig = requiredProperties.stream()
                .filter(prop -> environment.getProperty(prop) == null)
                .collect(Collectors.toList());

        if (!missingConfig.isEmpty()) {
            throw new RuntimeException("Missing required configuration properties: " + String.join(", ", missingConfig));
        }

        // Store the property values after validation
        this.authUrl = environment.getProperty("keycloak.auth.server");
        this.realm = environment.getProperty("keycloak.realm");
        this.clientId = environment.getProperty("keycloak.client.id");
        this.clientSecret = environment.getProperty("keycloak.client.secret");

        isConfigValidated = true;
    }


    public boolean validateToken(HttpServletRequest request) {
        if (!isConfigValidated) {
            validateConfig();
        }

        LOGGER.info("Entry - validateToken Method in KeyCloackTokenValidationClient ");
        boolean validationResponse = false;
        final String authorizationHeaderValue = request.getHeader("Authorization");
        if (authorizationHeaderValue != null && authorizationHeaderValue.startsWith("Bearer")) ;
        String token = authorizationHeaderValue.substring(7, authorizationHeaderValue.length());

        String url = authUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";


        OkHttpClient client = new OkHttpClient().newBuilder().hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;  // Always returns true, effectively disabling hostname verification
            }
        }).build();
        MediaType mediaType = MediaType.parse(APPLICATION_URL_FORM_ENCODED);

        RequestBody body = RequestBody.create(mediaType, "token=" + token + "&client_id=" + clientId + "&client_secret=" + clientSecret);

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
            if(clientResponse.body()!=null) {
                String response = clientResponse.body().string();

                JSONObject jsonObj = new JSONObject(response);
                validationResponse = (boolean) jsonObj.get("active");
            }
            LOGGER.info("Access Token Validation Status ::::" + validationResponse);
        } catch (IOException e) {
            LOGGER.info("Exception - validateToken Method in KeyCloackTokenValidationClient", e);
            e.printStackTrace();
        }
        LOGGER.info("Exit - validateToken Method in KeyCloackTokenValidationClient ");
        return validationResponse;

    }

    public static Properties fetchProperties() {
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