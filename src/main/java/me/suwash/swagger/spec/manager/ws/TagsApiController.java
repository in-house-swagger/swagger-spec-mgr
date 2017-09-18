package me.suwash.swagger.spec.manager.ws;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import io.swagger.annotations.ApiParam;

import java.util.List;

import me.suwash.swagger.spec.manager.ap.TagFacade;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.sv.domain.Tag;
import me.suwash.swagger.spec.manager.ws.gen.TagsApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

// TODO レスポンスはObjectじゃゆるいかな？
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-08-08T09:16:20.502+09:00")
@Controller
public class TagsApiController extends BaseApiController implements TagsApi {

    @Autowired
    private TagFacade facade;

    @Override
    public ResponseEntity<Identifiable<Link>> getTags(
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "email address for commit") @RequestHeader(value = "x-commit-email", required = false) final String commitEmail) {

        final CommitInfo commitInfo = commitInfo(commitUser, commitEmail);
        final List<String> idList = facade.idList(commitInfo);

        final ResourceSupport resource = new ResourceSupport();
        if (idList.isEmpty())
            return new ResponseEntity<Identifiable<Link>>(resource, HttpStatus.NO_CONTENT);

        for (final String curId : idList)
            resource.add(linkTo(methodOn(this.getClass()).getTagById(commitUser, commitEmail, curId)).withSelfRel());
        return new ResponseEntity<Identifiable<Link>>(resource, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> getTagById(
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "email address for commit") @RequestHeader(value = "x-commit-email", required = false) final String commitEmail,
        @ApiParam(value = "ID of tag to return", required = true) @PathVariable("tag") final String tag) {

        final CommitInfo commitInfo = commitInfo(commitUser, commitEmail);
        final Tag finded = facade.findById(commitInfo, tag);
        // TODO ws.modelを用意してmappingかな。
        return ResponseEntity.ok(finded);
    }

    @Override
    public ResponseEntity<Object> addTagWithId(
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "email address for commit") @RequestHeader(value = "x-commit-email", required = false) final String commitEmail,
        @ApiParam(value = "message for tag") @RequestHeader(value = "x-commit-message", required = false) final String commitMessage,
        @ApiParam(value = "ID of tag that needs to be add", required = true) @PathVariable("tag") final String tag,
        @ApiParam(value = "the SHA of the git object this is tagging", required = true) @RequestParam(value="object", required=true) final String object) {

        final CommitInfo commitInfo = commitInfo(commitUser, commitEmail, commitMessage);
        final Tag added = facade.add(commitInfo, object, tag);
        // TODO ws.modelを用意してmappingかな。
        return new ResponseEntity<Object>(added, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> renameTagWithId(
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "email address for commit") @RequestHeader(value = "x-commit-email", required = false) final String commitEmail,
        @ApiParam(value = "target ID of tag that needs to be update", required = true) @PathVariable("tag") final String fromTag,
        @ApiParam(value = "new ID of tag that needs to be update", required = true) @RequestParam(value="to", required=true) final String toTag) {

        final CommitInfo commitInfo = commitInfo(commitUser, commitEmail);
        final Tag updated = facade.rename(commitInfo, fromTag, toTag);
        // TODO ws.modelを用意してmappingかな。
        return new ResponseEntity<Object>(updated, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteTagById(
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "email address for commit") @RequestHeader(value = "x-commit-email", required = false) final String commitEmail,
        @ApiParam(value = "ID of tag to delete", required = true) @PathVariable("tag") final String tag) {

        final CommitInfo commitInfo = commitInfo(commitUser, commitEmail);
        facade.delete(commitInfo, tag);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

}
