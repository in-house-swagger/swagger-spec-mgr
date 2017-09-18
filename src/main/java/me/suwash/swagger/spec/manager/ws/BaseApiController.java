package me.suwash.swagger.spec.manager.ws;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.i18n.SpecMgrMessageSource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@lombok.extern.slf4j.Slf4j
public class BaseApiController {
    @ExceptionHandler
    public Object handleException(final SpecMgrException e) {
        log.error(
            SpecMgrMessageSource.getInstance().getMessage(
                MessageConst.ERRORHANDLE,
                array(e.getStackTrace()[0].toString(), e.getMessage())
                )
            );

        final String messageId = e.getMessageId();
        if (MessageConst.CHECK_NOTNULL.equals(messageId))
            return new ResponseEntity<Object>(e.getMessage(), HttpStatus.BAD_REQUEST);
        if (MessageConst.DATA_NOT_EXIST.equals(messageId))
            return new ResponseEntity<Object>(e.getMessage(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected CommitInfo commitInfo(final String user, final String email) {
        return commitInfo(user, email, null);
    }
    protected CommitInfo commitInfo(final String user, final String email, final String message) {
        return new CommitInfo(user, email, message);
    }

}
