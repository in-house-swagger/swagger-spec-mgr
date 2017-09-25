package me.suwash.swagger.spec.manager.ws;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import io.swagger.annotations.ApiParam;

import java.util.List;

import me.suwash.swagger.spec.manager.ap.SpecFacade;
import me.suwash.swagger.spec.manager.infra.config.ScmInfo;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.i18n.SpecMgrMessageSource;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.ws.gen.SpecsApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-08-08T09:16:20.502+09:00")
@Controller
@lombok.extern.slf4j.Slf4j
public class SpecsApiController implements SpecsApi {

    @Autowired
    private SpecFacade facade;

    private Object[] array(final Object... args) {
        return args;
    }

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

    private ScmInfo scmInfo(final String scmUser, final String scmEmail) {
        return new ScmInfo(scmUser, scmEmail);
    }

    @Override
    public ResponseEntity<Identifiable<Link>> getSpecs(
        @ApiParam(value = "user name for scm commit") @RequestHeader(value = "x-scm-user", required = false) final String scmUser,
        @ApiParam(value = "email address for scm commit") @RequestHeader(value = "x-scm-email", required = false) final String scmEmail) {

        final ScmInfo scmInfo = scmInfo(scmUser, scmEmail);
        final List<String> idList = facade.idList(scmInfo);

        final ResourceSupport resource = new ResourceSupport();
        if (idList.isEmpty())
            return new ResponseEntity<Identifiable<Link>>(resource, HttpStatus.NO_CONTENT);

        for (final String curId : idList)
            resource.add(linkTo(methodOn(this.getClass()).getSpecById(scmUser, scmEmail, curId)).withSelfRel());
        return new ResponseEntity<Identifiable<Link>>(resource, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> getSpecById(
        @ApiParam(value = "user name for scm commit") @RequestHeader(value = "x-scm-user", required = false) final String scmUser,
        @ApiParam(value = "email address for scm commit") @RequestHeader(value = "x-scm-email", required = false) final String scmEmail,
        @ApiParam(value = "ID of specification to return", required = true) @PathVariable("specId") final String specId) {

        final ScmInfo scmInfo = scmInfo(scmUser, scmEmail);
        final Spec finded = facade.findById(specId, scmInfo);
        return ResponseEntity.ok(finded.getPayload());
    }

    @Override
    public ResponseEntity<Object> addSpecWithId(
        @ApiParam(value = "user name for scm commit") @RequestHeader(value = "x-scm-user", required = false) final String scmUser,
        @ApiParam(value = "email address for scm commit") @RequestHeader(value = "x-scm-email", required = false) final String scmEmail,
        @ApiParam(value = "ID of specification that needs to be add", required = true) @PathVariable("specId") final String specId,
        @ApiParam(value = "Specification object that needs to be add", required = true) @RequestBody final Object payload) {

        final ScmInfo scmInfo = scmInfo(scmUser, scmEmail);
        final Spec added = facade.add(specId, payload, scmInfo);
        return new ResponseEntity<Object>(added.getPayload(), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> updateSpecWithId(
        @ApiParam(value = "user name for scm commit") @RequestHeader(value = "x-scm-user", required = false) final String scmUser,
        @ApiParam(value = "email address for scm commit") @RequestHeader(value = "x-scm-email", required = false) final String scmEmail,
        @ApiParam(value = "ID of specification that needs to be update", required = true) @PathVariable("specId") final String specId,
        @ApiParam(value = "Specification object that needs to be update", required = true) @RequestBody final Object payload) {

        final ScmInfo scmInfo = scmInfo(scmUser, scmEmail);
        final Spec updated = facade.update(specId, payload, scmInfo);
        return new ResponseEntity<Object>(updated.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteSpecById(
        @ApiParam(value = "user name for scm commit") @RequestHeader(value = "x-scm-user", required = false) final String scmUser,
        @ApiParam(value = "email address for scm commit") @RequestHeader(value = "x-scm-email", required = false) final String scmEmail,
        @ApiParam(value = "ID of specification to delete", required = true) @PathVariable("specId") final String specId) {

        final ScmInfo scmInfo = scmInfo(scmUser, scmEmail);
        facade.delete(specId, scmInfo);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

}
