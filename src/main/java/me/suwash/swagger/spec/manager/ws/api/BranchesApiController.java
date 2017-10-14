package me.suwash.swagger.spec.manager.ws.api;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.annotations.ApiParam;
import me.suwash.swagger.spec.manager.ap.dto.BranchDto;
import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.facade.BranchFacade;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.ws.api.gen.BranchesApi;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiController;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiModelMapper.OperationType;
import me.suwash.swagger.spec.manager.ws.mapper.BranchesApiModelMapper;
import me.suwash.swagger.spec.manager.ws.mapper.IdListApiModelMapper;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen",
    date = "2017-08-08T09:16:20.502+09:00")
@Controller
public class BranchesApiController extends BaseApiController implements BranchesApi {

  @Autowired
  private BranchFacade facade;

  private String getRealBranch(final String parsedBranch, final HttpServletRequest request) {
    final String extractPath = extractPathFromPattern(request);
    if (StringUtils.isEmpty(extractPath))
      return parsedBranch;
    return parsedBranch + "/" + extractPath;
  }

  @Override
  public ResponseEntity<Object> getBranches(final HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "X-Commit-User",
          required = false) final String commitUser) {

    final CommitInfo commitInfo = commitInfo(commitUser);
    final IdListDto dto = facade.idList(commitInfo);

    final IdListApiModelMapper mapper = new IdListApiModelMapper(dto);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> getBranchById(final HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "X-Commit-User",
          required = false) final String commitUser,
      @ApiParam(value = "ID of branch to return",
          required = true) @PathVariable("branch") final String branch) {

    final CommitInfo commitInfo = commitInfo(commitUser);
    final BranchDto dto = facade.findById(commitInfo, getRealBranch(branch, request));

    final BranchesApiModelMapper mapper = new BranchesApiModelMapper(dto, OperationType.READ);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> addBranchWithId(final HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "X-Commit-User",
          required = false) final String commitUser,
      @ApiParam(value = "ID of branch that needs to be add",
          required = true) @PathVariable("branch") final String branch,
      @ApiParam(value = "the SHA of the git object this is branching",
          required = true) @RequestParam(value = "object", required = true) final String object) {

    final CommitInfo commitInfo = commitInfo(commitUser);
    final BranchDto dto = facade.add(commitInfo, object, getRealBranch(branch, request));

    final BranchesApiModelMapper mapper = new BranchesApiModelMapper(dto, OperationType.CREATE);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> renameBranchWithId(final HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "X-Commit-User",
          required = false) final String commitUser,
      @ApiParam(value = "target ID of branch that needs to be update",
          required = true) @PathVariable("branch") final String fromBranch,
      @ApiParam(value = "new ID of branch that needs to be update",
          required = true) @RequestParam(value = "to", required = true) final String toBranch) {

    final CommitInfo commitInfo = commitInfo(commitUser);
    final BranchDto dto = facade.rename(commitInfo, getRealBranch(fromBranch, request), toBranch);

    final BranchesApiModelMapper mapper = new BranchesApiModelMapper(dto, OperationType.RENAME);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> deleteBranchById(final HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "X-Commit-User",
          required = false) final String commitUser,
      @ApiParam(value = "ID of branch to delete",
          required = true) @PathVariable("branch") final String branch) {

    final CommitInfo commitInfo = commitInfo(commitUser);
    final BranchDto dto = facade.delete(commitInfo, getRealBranch(branch, request));

    final BranchesApiModelMapper mapper = new BranchesApiModelMapper(dto, OperationType.DELETE);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> mergeBranch(final HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "X-Commit-User",
          required = false) final String commitUser,
      @ApiParam(value = "source ID of branch that needs to be merge",
          required = true) @RequestParam(value = "source",
              required = true) final String sourceBranch,
      @ApiParam(value = "target ID of branch that needs to be merge",
          required = true) @RequestParam(value = "target",
              required = true) final String targetBranch) {

    final CommitInfo commitInfo = commitInfo(commitUser);
    final BranchDto dto = facade.mergeBranch(commitInfo, sourceBranch, targetBranch);

    final BranchesApiModelMapper mapper = new BranchesApiModelMapper(dto, OperationType.UPDATE);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> switchBranch(final HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "X-Commit-User",
          required = false) final String commitUser,
      @ApiParam(value = "ID of branch to switch",
          required = true) @PathVariable("branch") final String branch) {

    final CommitInfo commitInfo = commitInfo(commitUser);
    final BranchDto dto = facade.switchBranch(commitInfo, getRealBranch(branch, request));

    final BranchesApiModelMapper mapper = new BranchesApiModelMapper(dto, OperationType.UPDATE);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

}
