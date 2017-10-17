package me.suwash.swagger.spec.manager.ws.infra;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.MSGCD_ERRORHANDLE;
import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerMapping;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.i18n.SpecMgrMessageSource;

@lombok.extern.slf4j.Slf4j
public abstract class BaseApiController {

  /**
   * 例外に合わせたレスポンスを返します。
   *
   * @param e 例外
   * @return 例外に合わせたレスポンス
   */
  @ExceptionHandler
  public Object handleException(final Exception e) {
    if (e instanceof HttpMessageNotReadableException
        || e instanceof MissingServletRequestParameterException) {
      final String message = e.getMessage().substring(0, e.getMessage().indexOf(':'));
      return new ResponseEntity<Object>(message, HttpStatus.BAD_REQUEST);
    }

    log.error(SpecMgrMessageSource.getInstance().getMessage(MSGCD_ERRORHANDLE,
        array(e.getStackTrace()[0].toString(), e.getMessage())), e);

    return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  protected CommitInfo commitInfo(final String user) {
    return commitInfo(user, null, null);
  }

  protected CommitInfo commitInfo(final String user, final String email) {
    return commitInfo(user, email, null);
  }

  protected CommitInfo commitInfo(final String user, final String email, final String message) {
    return new CommitInfo(urlDecode(user), urlDecode(email), urlDecode(message));
  }

  private String urlDecode(final String value) {
    if (StringUtils.isEmpty(value))
      return value;

    try {
      return URLDecoder.decode(value, "utf8");
    } catch (UnsupportedEncodingException e) {
      throw new SpecMgrException(MSGCD_ERRORHANDLE, array("URL Decode", e.getMessage()));
    }
  }

  /**
   * リクエストURIから、今回マッチしたパターンを取り除いた文字列を返します。
   *
   * @param request リクエスト
   * @return マッチしなかった、URIの部分文字列
   */
  protected String extractPathFromPattern(final HttpServletRequest request) {
    if (request == null)
      return null;
    String path =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    String bestMatchPattern =
        (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
  }
}
