package cn.bupt.edu.zfq.reggietakeout.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;


/**
 * 检查用户是否已经完成登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    // 路径匹配器, 支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public static final String[] urls = new String[]{
            "/employee/login", "/employee/login", "/backend/**", "/front/**",
            "/user/sendMsg", "/user/login",
            "/user/sendMsg", // 移动端发送短信
            "/user/login" // 移动端登录
    }; // 这些路径不需要处理

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws ServletException, java.io.IOException {
        var request = (HttpServletRequest) servletRequest;
        var response = (HttpServletResponse) servletResponse;
        var requestURI = request.getRequestURI();

        if (check(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getSession().getAttribute("employee") != null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getSession().getAttribute("user") != null) { // 移动端用户
            filterChain.doFilter(request, response);
            return;
        }

        log.info("用户未登录");
        // 通过输出流方式向客户端页面相应数据
        var objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(R.error("NOTLOGIN")));
    }

    /**
     * 路径匹配, 检查本次请求是否需要放行
     */
    public boolean check(String requestURI) {
        for (var url : urls)
            if (PATH_MATCHER.match(url, requestURI))
                return true;
        return false;
    }
}
