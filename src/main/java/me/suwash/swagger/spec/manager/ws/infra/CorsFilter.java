package me.suwash.swagger.spec.manager.ws.infra;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import me.suwash.swagger.spec.manager.infra.config.ApplicationProperties;

@Component
public class CorsFilter implements javax.servlet.Filter {
  @Autowired
  private ApplicationProperties props;

  private String allowOrigin;
  private String allowMethods;
  private String allowHeaders;
  private String getAllowMaxAge;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (StringUtils.isEmpty(allowOrigin)) {
      chain.doFilter(request, response);
      return;
    }

    HttpServletResponse res = (HttpServletResponse) response;
    res.addHeader("Access-Control-Allow-Origin", allowOrigin);
    res.addHeader("Access-Control-Allow-Methods", allowMethods);
    res.addHeader("Access-Control-Allow-Headers", allowHeaders);
    res.addHeader("Access-Control-Expose-Headers", allowHeaders);
    res.addHeader("Access-Control-Max-Age", getAllowMaxAge);
    chain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    if (StringUtils.isEmpty(props.getAllowOrigin()))
      return;

    allowOrigin = props.getAllowOrigin();
    allowMethods = props.getAllowMethods();
    allowHeaders = props.getAllowHeaders();
    getAllowMaxAge = props.getAllowMaxAge();
  }

  @Override
  public void destroy() {
    allowOrigin = null;
    allowMethods = null;
    allowHeaders = null;
  }
}
