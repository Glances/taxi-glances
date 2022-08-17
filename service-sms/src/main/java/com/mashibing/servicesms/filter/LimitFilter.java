package com.mashibing.servicesms.filter;


import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author 马士兵教育:晁鹏飞
 * @date
 */
@Component
public class LimitFilter implements Filter {

    // 2=每秒2个；0.1 = 10秒1个
    private static final RateLimiter RATE_LIMITER = RateLimiter.create(1);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        // 限流的业务逻辑
        if (RATE_LIMITER.tryAcquire()){
            filterChain.doFilter(servletRequest,servletResponse);
        }else {

            servletResponse.setCharacterEncoding("utf-8");
            servletResponse.setContentType("text/html; charset=utf-8");

            PrintWriter pw = null;

            pw = servletResponse.getWriter();
            pw.write("限流了");

            pw.close();


        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
