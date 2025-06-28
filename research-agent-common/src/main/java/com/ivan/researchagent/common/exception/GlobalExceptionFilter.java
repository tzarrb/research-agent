package com.ivan.researchagent.common.exception;

import com.ivan.researchagent.common.model.ErrorResponse;
import com.ivan.researchagent.common.utils.JacksonUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/28/周六
 **/
@Slf4j
public class GlobalExceptionFilter implements Filter {

    private static final String FILTER_NAME = "GlobalExceptionFilter";


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            log.warn("GlobalExceptionFilter::handle ex : ", e);

            int errorCode = ErrorEnum.SNEAK_AWAY.getCode();
            String errorMsg = ErrorEnum.SNEAK_AWAY.getMessage();
            String exMsg = e.getClass().getName();
            if (e.getCause() instanceof BizException) {
                errorCode = ((BizException) e.getCause()).getErrorInfo().getCode();
                errorMsg = e.getCause().getMessage();
                exMsg = e.getCause().getClass().getName();
            }

            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setCode(errorCode);
            errorResponse.setMessage(errorMsg);
            errorResponse.setTimestamp(new Date());
            errorResponse.setException(exMsg);
            errorResponse.setPath(httpServletRequest.getServletPath());

            // 把返回值输出到客户端
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            httpServletResponse.setStatus(HttpStatus.OK.value());
            ServletOutputStream out = httpServletResponse.getOutputStream();
            // 自身代码保证对象不为空，不会对外抛空指针异常
            out.write(Objects.requireNonNull(JacksonUtil.toJson(errorResponse)).getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        }
    }

    @Override
    public void destroy() {

    }

    public String getFilterName() {
        return FILTER_NAME;
    }

    public int getFilterOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
