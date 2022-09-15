package cn.zzx.reggie.filter;


import cn.zzx.reggie.common.BaseContext;
import cn.zzx.reggie.common.R;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 登录过滤器
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    // 路径匹配器，支持通配符
    public static final AntPathMatcher  PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        // 1.获取本次请求的URI
        String url =  request.getRequestURI();

        log.info("本次拦截的请求：{}",url);
        // 定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",  // 移动端发送短信
                "/user/login"    // 移动端用户登录

        };

        // 2. 判断请求是否需要处理
       boolean check = check(urls, url);

        // 3.如果不需要处理，则直接放行
        if(check){
            log.info("本次{}请求不需要处理",url);
            filterChain.doFilter(request,response);
            return;
        }

        // 4-1.判断用户登录状态，如果已经登录，则直接放行
        if (request.getSession().getAttribute("employee")!=null){
            log.info("用户已登录，id为:{}",request.getSession().getAttribute("employee"));

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            long id = Thread.currentThread().getId();
            log.info("线程id:{}", id);

            filterChain.doFilter(request,response);
            return;
        }

        //4-2、判断移动端用户登录状态，如果已经登录，则直接放行
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已经登录，用户id为：{}", request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        // 5.如果没有登录则返回未登录结果，通过输出流方式向客户端响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("用户未登录");
        return;

    }

    public boolean check(String[] urls,String requesturl){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url,requesturl);
               if (match){
                   return true;
            }
        }
        return false;
    }
}
