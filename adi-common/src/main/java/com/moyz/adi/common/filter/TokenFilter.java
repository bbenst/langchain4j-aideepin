package com.moyz.adi.common.filter;

import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.cosntant.RedisKeyConstant;
import com.moyz.adi.common.entity.User;
import com.moyz.adi.common.util.JsonUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.MessageFormat;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * 基于 Token 的请求鉴权过滤器。
 */
@Slf4j
@Component
public class TokenFilter extends OncePerRequestFilter {

    /**
     * 允许匿名访问的接口前缀。
     */
    protected static final String[] EXCLUDE_API = {
            "/auth/",
            "/model/",
            "/user/avatar/",
            "/draw/public/",
            "/draw/detail/",
            "/draw/comment/list",
            "/knowledge-base/public/",
            "/workflow/public",
            "/mcp/public",
            "/sys/config/public/",
    };

    /**
     * 从请求参数中读取 Token 的接口前缀。
     */
    protected static final String[] TOKEN_IN_PARAMS = {
            "/my-image/",
            "/my-thumbnail/",
            "/image/",
            "/file/"
    };

    /**
     * Redis 操作模板，用于读取用户会话信息。
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 应用上下文路径。
     */
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * 执行鉴权过滤逻辑。
     *
     * @param request 请求
     * @param response 响应
     * @param filterChain 过滤链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String token = request.getHeader(AUTHORIZATION);
        if (StringUtils.isBlank(token) && checkPathWithToken(requestUri)) {
            token = request.getParameter("token");
        }
        if (excludePath(requestUri)) {

            if (StringUtils.isNotBlank(token)) {
                String tokenKey = MessageFormat.format(RedisKeyConstant.USER_TOKEN, token);
                String userJson = stringRedisTemplate.opsForValue().get(tokenKey);
                if (StringUtils.isNotBlank(userJson)) {
                    User user = JsonUtil.fromJson(userJson, User.class);
                    if (null != user) {
                        ThreadContext.setCurrentUser(user);
                        ThreadContext.setToken(token);
                    }
                }
            }
            filterChain.doFilter(request, response);
        } else if (StringUtils.isNotBlank(token)) {
            String tokenKey = MessageFormat.format(RedisKeyConstant.USER_TOKEN, token);
            String userJson = stringRedisTemplate.opsForValue().get(tokenKey);
            if (StringUtils.isBlank(userJson)) {
                log.warn("未登录:{}", requestUri);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            User user = JsonUtil.fromJson(userJson, User.class);
            if (null == user) {
                log.warn("用户不存在:{}", requestUri);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            if (Boolean.TRUE.equals(!user.getIsAdmin()) && requestUri.startsWith("/admin/")) {
                log.warn("无管理权限:{}", requestUri);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            ThreadContext.setCurrentUser(user);
            ThreadContext.setToken(token);
            filterChain.doFilter(request, response);
        } else {
            log.warn("未授权:{}", requestUri);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * 判断是否为免鉴权路径。
     *
     * @param requestUri 请求路径
     * @return 是否免鉴权
     */
    private boolean excludePath(String requestUri) {
        for (String path : EXCLUDE_API) {
            if (requestUri.startsWith(contextPath + path)) {
                return true;
            }
        }
        for (String path : AdiConstant.WEB_RESOURCES) {
            if (requestUri.startsWith(contextPath + path) || requestUri.endsWith(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否允许从请求参数读取 Token。
     *
     * @param requestUri 请求路径
     * @return 是否允许
     */
    private boolean checkPathWithToken(String requestUri) {
        for (String path : TOKEN_IN_PARAMS) {
            if (requestUri.startsWith(contextPath + path)) {
                return true;
            }
        }
        return false;
    }
}
