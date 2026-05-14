package gov.state.hhs.auth;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet filter that guards all /staff/* pages.
 * Unauthenticated requests are redirected to the login page.
 */
@WebFilter("/staff/*")
public class AuthFilter implements Filter {

    @Inject
    private SessionBean sessionBean;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (sessionBean.isLoggedIn()) {
            chain.doFilter(request, response);
        } else {
            String contextPath = req.getContextPath();
            resp.sendRedirect(contextPath + "/login.xhtml");
        }
    }
}
