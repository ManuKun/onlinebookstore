package com.bittercode.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecurityHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;

        // 1. Prevent Clickjacking
        res.setHeader("X-Frame-Options", "DENY");

        // 2. Prevent MIME-sniffing vulnerabilities
        res.setHeader("X-Content-Type-Options", "nosniff");

        // 3. Content Security Policy
        res.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://ajax.googleapis.com https://code.jquery.com https://maxcdn.bootstrapcdn.com; " +
                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://maxcdn.bootstrapcdn.com; " +
                "font-src 'self' https://cdn.jsdelivr.net https://maxcdn.bootstrapcdn.com; " +
                "img-src 'self' data:;");

        // 4. Strict Transport Security
        res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // 5. SameSite Cookie Fix
        String sessionId = req.getSession().getId();

        // Use Lax for local development (Strict can break navigation)
        res.setHeader("Set-Cookie", "JSESSIONID=" + sessionId + "; HttpOnly; SameSite=Lax");

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}