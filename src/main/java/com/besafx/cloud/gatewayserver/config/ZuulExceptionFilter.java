package com.besafx.cloud.gatewayserver.config;

import com.besafx.cloud.common.exception.ApiError;
import com.besafx.cloud.common.util.JsonConverter;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class ZuulExceptionFilter extends ZuulFilter {

    protected static final String SEND_ERROR_FILTER_RAN = "sendErrorFilter.ran";

    @Override
    public String filterType() {
        return FilterConstants.ERROR_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.SEND_ERROR_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        Throwable ex = ctx.getThrowable();
        return ex instanceof ZuulException && !ctx.getBoolean(SEND_ERROR_FILTER_RAN, false);
    }

    @Override
    public Object run() {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            ZuulException ex = (ZuulException) ctx.getThrowable();

            // log this as error
            log.error(ExceptionUtils.getRootCauseMessage(ex));

            final HttpServletResponse response = ctx.getResponse();
            final ApiError apiError = new ApiError(
                    HttpStatus.BAD_GATEWAY,
                    ExceptionUtils.getRootCauseMessage(ex),
                    ExceptionUtils.getRootCauseMessage(ex),
                    "Exception from request: " + ctx.getRequest().getRequestURL().toString()
            );
            response.setStatus(apiError.getStatus().value());
            response.setContentType("application/json");
            response.getWriter().write(JsonConverter.toString(apiError));

            ctx.set(SEND_ERROR_FILTER_RAN, true);
        } catch (Exception ex) {
            log.error(ExceptionUtils.getMessage(ex));
        }
        return null;
    }
}
