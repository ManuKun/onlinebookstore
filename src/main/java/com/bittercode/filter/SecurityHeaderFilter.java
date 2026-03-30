package com.bittercode.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecurityHeaderFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletResponse res = (HttpServletResponse) response;

        // 1. Prevents Clickjacking
        res.setHeader("X-Frame-Options", "DENY");

        // 2. Prevents MIME-sniffing vulnerabilities
        res.setHeader("X-Content-Type-Options", "nosniff");

        // 3. Basic Content Security Policy to stop unauthorized scripts
        res.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';");

        // 4. Force HTTPS (if you were using SSL, but good for the report!)
        res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}