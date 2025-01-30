package org.sitenv.spring.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@Component
@WebFilter(urlPatterns = "/fhir/*")
public class TokenFilter implements Filter {

    /** The LOGGERGER */
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenFilter.class);
    private static final String AUTHORIZATION = "Authorization";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * (non-Javadoc) Details of the APIs and the request types that can be used with
     * this application
     *
     * @param req
     * @param res
     * @param chain
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        LOGGER.info("Entry - doFilter Method in TokenFilter ");
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        String path = request.getPathInfo();
        if ("/metadata".equals(path)||"/api/auth/generate-token".equals(path)) {
            chain.doFilter(request, response);
            LOGGER.info("Exit - doFilter Method in TokenFilter -- metadata endpoint");
        }



        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        KeyCloackTokenValidationClient keyCloackTokenValidationClient = context.getBean(KeyCloackTokenValidationClient.class);
        boolean responseStatus = keyCloackTokenValidationClient.validateToken(request);
        LOGGER.info("RESPONSE STATUS ::  " + responseStatus);

        if (responseStatus) {
            chain.doFilter(request, response);
            LOGGER.info("Exit - doFilter Method in TokenFilter ");
        } else {
            response.sendError(401, "UnAuthorized User");
            LOGGER.error("Error in doFilter TokenFilter - UnAuthorized User ");
        }
    }

    /**
     * Destroy method
     */
    @Override
    public void destroy() {
        LOGGER.info("Entry - destroy Method in Web Filter ");

    }

}
