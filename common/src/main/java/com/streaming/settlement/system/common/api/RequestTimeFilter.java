package com.streaming.settlement.system.common.api;

import jakarta.servlet.*;

import java.io.IOException;

public class RequestTimeFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            ResponseContext.setRequestAt(System.currentTimeMillis());
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            ResponseContext.clear();
        }
    }
}
