package com.besafx.cloud.gatewayserver.config;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Slf4j
@Component
public class AuthorizationFilter extends ZuulFilter {

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        final RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        Optional<String> authorizationHeader = Optional.ofNullable(request.getHeader("Authorization"));
        if (authorizationHeader.isPresent()) {
            final String token = authorizationHeader.get();
            log.info("Shared Authorization: {}", token);
            requestContext.addZuulRequestHeader("Authorization", token);
        } else {
            log.info("Authorization header not found.");
        }

        return null;
    }
}
