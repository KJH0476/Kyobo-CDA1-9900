package kyobo.cda.NotificationService.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.UUID;

@Component
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String email = request.getHeader("X-User-Email");
        MDC.put("email", Objects.requireNonNullElse(email, "unauthorized(anonymous) user"));

        String requestUri = request.getRequestURI();
        MDC.put("requestUri", requestUri);

        String requestId = generateRequestId(request);
        MDC.put("requestId", requestId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.clear();
    }

    private String generateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }
        return requestId;
    }
}
