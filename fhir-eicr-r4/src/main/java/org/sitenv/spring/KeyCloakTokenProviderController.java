package org.sitenv.spring;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import okhttp3.*;
import org.json.JSONObject;
import org.sitenv.spring.configuration.KeyCloackTokenValidationClient;
import org.sitenv.spring.exception.KeycloakCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;


@RestController
@RequestMapping("/api/auth")
public class KeyCloakTokenProviderController {


    private static final Logger LOGGER = LoggerFactory.getLogger(KeyCloackTokenValidationClient.class);
    public static final String TOKEN = "token";
    private static final String APPLICATION_URL_FORM_ENCODED = "application/x-www-form-urlencoded";

    @Autowired
    private Environment environment;



    private static final List<String> requiredProperties = List.of(
            "keycloak.auth.server",
            "keycloak.realm"
    );

    private boolean isConfigValidated = false;

    private String authUrl;
    private String realm;




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

        isConfigValidated = true;
    }

    @PostMapping("/generate-token")
    public Object generateToken(@RequestParam Map<String, Object> authenciationTokenDetails) throws IOException {
        LOGGER.info("Entry - validateToken Method in KeyCloakTokenValidationClient");

      if(!isConfigValidated)
      {
          validateConfig();
      }

        String url = String.format("%s/realms/%s/protocol/openid-connect/token", authUrl, realm);

        MediaType mediaType = MediaType.parse(APPLICATION_URL_FORM_ENCODED);

        StringBuilder authRequestBody = new StringBuilder();
        for (Map.Entry<String, Object> authEntry : authenciationTokenDetails.entrySet()) {
            authRequestBody
                    .append(authEntry.getKey())
                    .append("=")
                    .append(authEntry.getValue())
                    .append("&");
        }
        if (authRequestBody.length() > 0) {
            authRequestBody.setLength(authRequestBody.length() - 1);
        }
        RequestBody body = RequestBody.create(mediaType, authRequestBody.toString());

        Request requestOne =
                new Request.Builder()
                        .url(url)
                        .method("POST", body)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();

        OkHttpClient client = new OkHttpClient().newBuilder().hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;  // Always returns true, effectively disabling hostname verification
            }
        }).build();
        try (Response response = client.newCall(requestOne).execute()) {

            if (!response.isSuccessful()) {
                LOGGER.error("Failed to authenticate: {}", response.message());
                return false;
            }

            String responseBody = response.body() != null ? response.body().string() : "{}";
            JSONObject tokenResponse= new JSONObject(responseBody);

            if (tokenResponse != null) {
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(tokenResponse.toString());

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token validation failed.");
            }

        } catch (IOException e) {
            LOGGER.error("Exception - validateToken Method in KeyCloakTokenValidationClient", e);
            throw new IOException("Exception - validateToken Method in KeyCloakTokenValidationClient", e);
        } catch (Exception e) {

            throw new KeycloakCredentialsException(
                    "Exception - generate Method in KeyCloakTokenValidationClient" + e.getMessage());
        } finally {
            LOGGER.info("Exit - validateToken Method in KeyCloakTokenValidationClient");
        }
    }
}
