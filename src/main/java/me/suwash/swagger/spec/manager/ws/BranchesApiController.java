package me.suwash.swagger.spec.manager.ws;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import io.swagger.annotations.ApiParam;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import me.suwash.swagger.spec.manager.ap.BranchFacade;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.sv.domain.Branch;
import me.suwash.swagger.spec.manager.ws.gen.BranchesApi;

import org.apache.commons.lang3.StringUtils;
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
public class BranchesApiController extends BaseApiController implements BranchesApi {

    @Autowired
    private BranchFacade facade;

    @Override
    public ResponseEntity<Identifiable<Link>> getBranches(
        final HttpServletRequest request,
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "email address for commit") @RequestHeader(value = "x-commit-email", required = false) final String commitEmail) {

        final CommitInfo commitInfo = commitInfo(commitUser, commitEmail);
        final List<String> idList = facade.idList(commitInfo);

        final ResourceSupport resource = new ResourceSupport();
        if (idList.isEmpty())
            return new ResponseEntity<Identifiable<Link>>(resource, HttpStatus.NO_CONTENT);

        for (final String curId : idList)
            resource.add(linkTo(methodOn(this.getClass()).getBranchById(request, commitUser, commitEmail, curId)).withSelfRel());
        return new ResponseEntity<Identifiable<Link>>(resource, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> getBranchById(
        final HttpServletRequest request,
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "email address for commit") @RequestHeader(value = "x-commit-email", required = false) final String commitEmail,
        @ApiParam(value = "ID of branch to return", required = true) @PathVariable("branch") final String branch) {

        final CommitInfo commitInfo = commitInfo(commitUser, commitEmail);
        final Branch finded = facade.findById(commitInfo, getRealBranch(branch, request));
        // TODO ws.modelを用意してmappingかな。
        return ResponseEntity.ok(finded);
    }

    @Override
    public ResponseEntity<Object> addBranchWithId(
        final HttpServletRequest request,
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "email address for commit") @RequestHeader(value = "x-commit-email", required = false) final String commitEmail,
        @ApiParam(value = "ID of branch that needs to be add", required = true) @PathVariable("branch") final String branch,
        @ApiParam(value = "the SHA of the git object this is branching", required = true) @RequestParam(value="object", required=true) final String object) {

        final CommitInfo commitInfo = commitInfo(commitUser, commitEmail);
        final Branch added = facade.add(commitInfo, object, getRealBranch(branch, request));
        // TODO ws.modelを用意してmappingかな。
        return new ResponseEntity<Object>(added, HttpStatus.CREATED);
    }

    private String getRealBranch(final String parsedBranch, final HttpServletRequest request) {
        final String extractPath = extractPathFromPattern(request);
        if (StringUtils.isEmpty(extractPath)) return parsedBranch;
        return parsedBranch + "/" + extractPath;
    }

    @Override
    public ResponseEntity<Object> renameBranchWithId(
        final HttpServletRequest request,
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "email address for commit") @RequestHeader(value = "x-commit-email", required = false) final String commitEmail,
        @ApiParam(value = "target ID of branch that needs to be update", required = true) @PathVariable("branch") final String fromBranch,
        @ApiParam(value = "new ID of branch that needs to be update", required = true) @RequestParam(value="to", required=true) final String toBranch) {

        final CommitInfo commitInfo = commitInfo(commitUser, commitEmail);
        final Branch updated = facade.rename(commitInfo, getRealBranch(fromBranch, request), toBranch);
        // TODO ws.modelを用意してmappingかな。
        return new ResponseEntity<Object>(updated, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteBranchById(
        final HttpServletRequest request,
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "email address for commit") @RequestHeader(value = "x-commit-email", required = false) final String commitEmail,
        @ApiParam(value = "ID of branch to delete", required = true) @PathVariable("branch") final String branch) {

        final CommitInfo commitInfo = commitInfo(commitUser, commitEmail);
        facade.delete(commitInfo, getRealBranch(branch, request));
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> mergeBranch(
        final HttpServletRequest request,
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "email address for commit") @RequestHeader(value = "x-commit-email", required = false) final String commitEmail,
        @ApiParam(value = "source ID of branch that needs to be merge", required = true) @RequestParam(value="source", required=true) final String sourceBranch,
        @ApiParam(value = "target ID of branch that needs to be merge", required = true) @RequestParam(value="target", required=true) final String targetBranch) {

        final CommitInfo commitInfo = commitInfo(commitUser, commitEmail);
        final Branch merged = facade.mergeBranch(commitInfo, sourceBranch, targetBranch);
        // TODO ws.modelを用意してmappingかな。
        return new ResponseEntity<Object>(merged, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> switchBranch(
        final HttpServletRequest request,
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "email address for commit") @RequestHeader(value = "x-commit-email", required = false) final String commitEmail,
        @ApiParam(value = "ID of branch to switch", required = true) @PathVariable("branch") final String branch) {

        final CommitInfo commitInfo = commitInfo(commitUser, commitEmail);
        final Branch switched = facade.switchBranch(commitInfo, branch);
        // TODO ws.modelを用意してmappingかな。
        return new ResponseEntity<Object>(switched, HttpStatus.OK);
    }

}
