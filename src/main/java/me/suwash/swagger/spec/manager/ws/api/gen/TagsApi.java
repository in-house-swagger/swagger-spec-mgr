/**
 * NOTE: This class is auto generated by the swagger code generator program (2.2.3).
 * https://github.com/swagger-api/swagger-codegen Do not edit the class manually.
 */

package me.suwash.swagger.spec.manager.ws.api.gen;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import me.suwash.swagger.spec.manager.ws.model.gen.IdListApiModelGen;
import me.suwash.swagger.spec.manager.ws.model.gen.TagsApiModelGen;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen",
    date = "2017-08-08T21:14:16.911+09:00")
@Api(value = "tags", description = "Git Tag Management API")
public interface TagsApi {

  @ApiOperation(value = "/tags GET", notes = "Returns all tags", response = IdListApiModelGen.class,
      tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "successful operation",
          response = IdListApiModelGen.class),
      @ApiResponse(code = 404, message = "Tag not found", response = Void.class)})
  @RequestMapping(value = "/tags", produces = {"application/json"}, method = RequestMethod.GET)
  ResponseEntity<Object> getTags(HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user",
          required = false) final String commitUser);

  @ApiOperation(value = "/tags/{tag} GET", notes = "Returns a single tag",
      response = TagsApiModelGen.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "successful operation", response = TagsApiModelGen.class),
      @ApiResponse(code = 400, message = "Invalid ID supplied", response = Void.class),
      @ApiResponse(code = 404, message = "Tag not found", response = Void.class)})
  @RequestMapping(value = {"/tags/{tag:.+}", "/tags/{tag:.+}/**"}, produces = {"application/json"},
      method = RequestMethod.GET)
  ResponseEntity<Object> getTagById(HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user",
          required = false) final String commitUser,
      @ApiParam(value = "ID of tag to return",
          required = true) @PathVariable("tag") final String tag);

  @ApiOperation(value = "/tags/{tag} POST", notes = "", response = TagsApiModelGen.class, tags = {})
  @ApiResponses(
      value = {@ApiResponse(code = 405, message = "Invalid input", response = Void.class)})
  @RequestMapping(value = {"/tags/{tag:.+}", "/tags/{tag:.+}/**"}, produces = {"application/json"},
      consumes = {"application/json"}, method = RequestMethod.POST)
  ResponseEntity<Object> addTagWithId(HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user",
          required = false) final String commitUser,
      @ApiParam(value = "message for tag") @RequestHeader(value = "x-commit-message",
          required = false) final String commitMessage,
      @ApiParam(value = "ID of tag that needs to be add",
          required = true) @PathVariable("tag") final String tag,
      @ApiParam(value = "the SHA of the git object this is tagging",
          required = true) @RequestParam(value = "object", required = true) final String object);

  @ApiOperation(value = "/tags/{tag} PUT", notes = "", response = TagsApiModelGen.class, tags = {})
  @ApiResponses(
      value = {@ApiResponse(code = 400, message = "Invalid ID supplied", response = Void.class),
          @ApiResponse(code = 404, message = "Tag not found", response = Void.class),
          @ApiResponse(code = 405, message = "Validation exception", response = Void.class)})
  @RequestMapping(value = {"/tags/{tag:.+}", "/tags/{tag:.+}/**"}, produces = {"application/json"},
      consumes = {"application/json"}, method = RequestMethod.PUT)
  ResponseEntity<Object> renameTagWithId(HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user",
          required = false) final String commitUser,
      @ApiParam(value = "target ID of tag that needs to be update",
          required = true) @PathVariable("tag") final String fromTag,
      @ApiParam(value = "new ID of tag that needs to be update",
          required = true) @RequestParam(value = "to", required = true) final String toTag);

  @ApiOperation(value = "/tags/{tag} DELETE", notes = "", response = Void.class, tags = {})
  @ApiResponses(
      value = {@ApiResponse(code = 400, message = "Invalid ID supplied", response = Void.class),
          @ApiResponse(code = 404, message = "Tag not found", response = Void.class)})
  @RequestMapping(value = {"/tags/{tag:.+}", "/tags/{tag:.+}/**"}, produces = {"application/json"},
      method = RequestMethod.DELETE)
  ResponseEntity<Object> deleteTagById(HttpServletRequest request,
      @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user",
          required = false) final String commitUser,
      @ApiParam(value = "ID of tag to delete",
          required = true) @PathVariable("tag") final String tag);

}
