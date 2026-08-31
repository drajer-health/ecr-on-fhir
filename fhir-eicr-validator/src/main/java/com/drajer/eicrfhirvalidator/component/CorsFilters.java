package com.drajer.eicrfhirvalidator.component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * Servlet filter that adds permissive CORS headers to every response and short-circuits
 * {@code OPTIONS} preflight requests with a {@code 200 OK}.
 */
@Component
public  class CorsFilters implements Filter {

    /**
     * Sets CORS response headers, then either responds directly to {@code OPTIONS} preflight
     * requests or passes the request along the filter chain.
     *
     * @param req the incoming servlet request
     * @param res the servlet response to add CORS headers to
     * @param chain the remaining filter chain
     * @throws IOException if an I/O error occurs during filtering
     * @throws ServletException if an error occurs during filtering
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }

    public void init(FilterConfig filterConfig) {
    }

    public void destroy() {
    }
}