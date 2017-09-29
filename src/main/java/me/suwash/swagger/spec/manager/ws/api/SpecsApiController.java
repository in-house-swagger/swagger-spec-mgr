package me.suwash.swagger.spec.manager.ws.api;

import io.swagger.annotations.ApiParam;
import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.dto.SpecDto;
import me.suwash.swagger.spec.manager.ap.facade.SpecFacade;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.ws.api.gen.SpecsApi;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiController;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiModelMapper.OperationType;
import me.suwash.swagger.spec.manager.ws.mapper.IdListApiModelMapper;
import me.suwash.swagger.spec.manager.ws.mapper.SpecsApiModelMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-08-08T09:16:20.502+09:00")
@Controller
public class SpecsApiController extends BaseApiController implements SpecsApi {

    @Autowired
    private SpecFacade facade;

    @Override
    public ResponseEntity<Object> getSpecs(
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser) {

        final CommitInfo commitInfo = commitInfo(commitUser);
        final IdListDto dto = facade.idList(commitInfo);

        final IdListApiModelMapper mapper = new IdListApiModelMapper(dto);
        return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
    }

    @Override
    public ResponseEntity<Object> getSpecById(
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "ID of specification to return", required = true) @PathVariable("specId") final String specId) {

        final CommitInfo commitInfo = commitInfo(commitUser);
        final SpecDto dto = facade.findById(commitInfo, specId);

        final SpecsApiModelMapper mapper = new SpecsApiModelMapper(dto, OperationType.READ);
        return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
    }

    @Override
    public ResponseEntity<Object> addSpecWithId(
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "message for commit") @RequestHeader(value = "x-commit-message", required = false) final String commitMessage,
        @ApiParam(value = "ID of specification that needs to be add", required = true) @PathVariable("specId") final String specId,
        @ApiParam(value = "Specification object that needs to be add", required = true) @RequestBody final Object payload) {

        final CommitInfo commitInfo = commitInfo(commitUser, null, commitMessage);
        final SpecDto dto = facade.add(commitInfo, specId, payload);

        final SpecsApiModelMapper mapper = new SpecsApiModelMapper(dto, OperationType.CREATE);
        return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
    }

    @Override
    public ResponseEntity<Object> updateSpecWithId(
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "ID of specification that needs to be update", required = true) @PathVariable("specId") final String specId,
        @ApiParam(value = "Specification object that needs to be update", required = true) @RequestBody final Object payload) {

        final CommitInfo commitInfo = commitInfo(commitUser);
        final SpecDto dto = facade.update(commitInfo, specId, payload);

        final SpecsApiModelMapper mapper = new SpecsApiModelMapper(dto, OperationType.UPDATE);
        return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
    }

    @Override
    public ResponseEntity<Void> deleteSpecById(
        @ApiParam(value = "user name for commit") @RequestHeader(value = "x-commit-user", required = false) final String commitUser,
        @ApiParam(value = "ID of specification to delete", required = true) @PathVariable("specId") final String specId) {

        final CommitInfo commitInfo = commitInfo(commitUser);
        final SpecDto dto = facade.delete(commitInfo, specId);

        final SpecsApiModelMapper mapper = new SpecsApiModelMapper(dto, OperationType.DELETE);
        return new ResponseEntity<Void>(mapper.getHttpStatus());
    }

}
