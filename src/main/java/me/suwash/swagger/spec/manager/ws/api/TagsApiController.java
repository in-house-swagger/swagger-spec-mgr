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
import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.dto.TagDto;
import me.suwash.swagger.spec.manager.ap.facade.TagFacade;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.ws.api.gen.TagsApi;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiController;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiModelMapper.OperationType;
import me.suwash.swagger.spec.manager.ws.mapper.IdListApiModelMapper;
import me.suwash.swagger.spec.manager.ws.mapper.TagsApiModelMapper;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen",
    date = "2017-08-08T09:16:20.502+09:00")
@Controller
public class TagsApiController extends BaseApiController implements TagsApi {

  @Autowired
  private TagFacade facade;

  private String getRealTag(final String parsedTag, final HttpServletRequest request) {
    final String extractPath = extractPathFromPattern(request);
    if (StringUtils.isEmpty(extractPath))
      return parsedTag;
    return parsedTag + "/" + extractPath;
  }

  @Override
  public ResponseEntity<Object> getTags(final HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "X-Commit-User",
          required = false) final String commitUser) {

    final CommitInfo commitInfo = commitInfo(commitUser);
    final IdListDto dto = facade.idList(commitInfo);

    final IdListApiModelMapper mapper = new IdListApiModelMapper(dto);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> getTagById(final HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "X-Commit-User",
          required = false) final String commitUser,
      @ApiParam(value = "ID of tag to return",
          required = true) @PathVariable("tag") final String tag) {

    final CommitInfo commitInfo = commitInfo(commitUser);
    final TagDto dto = facade.findById(commitInfo, getRealTag(tag, request));

    final TagsApiModelMapper mapper = new TagsApiModelMapper(dto, OperationType.READ);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> addTagWithId(final HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "X-Commit-User",
          required = false) final String commitUser,
      @ApiParam(value = "message for tag") @RequestHeader(value = "X-Commit-Message",
          required = false) final String commitMessage,
      @ApiParam(value = "ID of tag that needs to be add",
          required = true) @PathVariable("tag") final String tag,
      @ApiParam(value = "the SHA of the git object this is tagging",
          required = true) @RequestParam(value = "object", required = true) final String object) {

    final CommitInfo commitInfo = commitInfo(commitUser, null, commitMessage);
    final TagDto dto = facade.add(commitInfo, object, getRealTag(tag, request));

    final TagsApiModelMapper mapper = new TagsApiModelMapper(dto, OperationType.CREATE);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> renameTagWithId(final HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "X-Commit-User",
          required = false) final String commitUser,
      @ApiParam(value = "target ID of tag that needs to be update",
          required = true) @PathVariable("tag") final String fromTag,
      @ApiParam(value = "new ID of tag that needs to be update",
          required = true) @RequestParam(value = "to", required = true) final String toTag) {

    final CommitInfo commitInfo = commitInfo(commitUser);
    final TagDto dto = facade.rename(commitInfo, getRealTag(fromTag, request), toTag);

    final TagsApiModelMapper mapper = new TagsApiModelMapper(dto, OperationType.RENAME);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> deleteTagById(final HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "X-Commit-User",
          required = false) final String commitUser,
      @ApiParam(value = "ID of tag to delete",
          required = true) @PathVariable("tag") final String tag) {

    final CommitInfo commitInfo = commitInfo(commitUser);
    final TagDto dto = facade.delete(commitInfo, getRealTag(tag, request));

    final TagsApiModelMapper mapper = new TagsApiModelMapper(dto, OperationType.DELETE);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

}
