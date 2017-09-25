package me.suwash.swagger.spec.manager.ws.infra;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;

import javax.servlet.http.HttpServletRequest;

import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.i18n.SpecMgrMessageSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerMapping;

@lombok.extern.slf4j.Slf4j
public abstract class BaseApiController {
    @Autowired
    private SpecMgrContext context;

    @ExceptionHandler
    public Object handleException(final Exception e) {
        if (e instanceof HttpMessageNotReadableException ||
            e instanceof MissingServletRequestParameterException) {
            final String message = e.getMessage().substring(0, e.getMessage().indexOf(':'));
            return new ResponseEntity<Object>(message, HttpStatus.BAD_REQUEST);
        }

        log.error(
            SpecMgrMessageSource.getInstance().getMessage(
                MessageConst.ERRORHANDLE,
                array(e.getStackTrace()[0].toString(), e.getMessage())
                )
            , e);

        return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected CommitInfo commitInfo(final String user, final String email) {
        return commitInfo(user, email, null);
    }

    protected CommitInfo commitInfo(final String user, final String email, final String message) {
        return new CommitInfo(user, email, message);
    }

    /**
     *
     * リクエストURIから、今回マッチしたパターンを取り除いた文字列を返します。
     *
     * @param request リクエスト
     * @return マッチしなかった、URIの部分文字列
     */
    protected String extractPathFromPattern(final HttpServletRequest request) {
        if (request == null) return null;
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
    }
}
