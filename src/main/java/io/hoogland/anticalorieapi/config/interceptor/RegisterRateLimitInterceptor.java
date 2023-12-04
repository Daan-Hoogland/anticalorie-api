package io.hoogland.anticalorieapi.config.interceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.hoogland.anticalorieapi.jwt.AuthTokenFilter;
import io.hoogland.anticalorieapi.utils.IpAddrUtils;
import io.hoogland.anticalorieapi.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RegisterRateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RegisterRateLimitInterceptor.class);


    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Autowired
    private JwtUtils jwtUtils;

    private Bucket newBucket(String identifier) {
        Bandwidth limit = Bandwidth.builder().capacity(1).refillGreedy(5, Duration.ofMinutes(1)).build();
        return Bucket.builder().addLimit(limit).build();
    }

    public Bucket getBucket(String identifier) {
        return cache.computeIfAbsent(identifier, this::newBucket);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String identifier = "";
        // Check if JWT is present and valid, else get ip addr as identifier.
        String jwt = jwtUtils.parseJwt(request);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            identifier = jwt;
        } else {
            String ipAddr = IpAddrUtils.getIpAddressForRequest(request);
            if (ipAddr == null || ipAddr.isEmpty()) {
                // Can't find ip addr
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing header: remoteIpAddress");
                return false;
            }
            identifier = ipAddr;
        }

        Bucket bucket = getBucket(identifier);
        request.setAttribute("identifier", identifier);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long timeTillRefill = probe.getNanosToWaitForRefill() / 1000000000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(timeTillRefill));
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                    "You have exhausted your API Request Quota");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Add token back to pool if the status returns http400 to avoid throttling whilst registering or submitting forms with errors.
        String identifier = (String) request.getAttribute("identifier");
        if(StringUtils.isNotBlank(identifier) && request.getAttribute("org.springframework.web.servlet.View.responseStatus").equals(HttpStatus.BAD_REQUEST)) {
            getBucket(identifier).addTokens(1);
        }
    }
}
