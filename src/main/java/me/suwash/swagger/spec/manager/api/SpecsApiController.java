package me.suwash.swagger.spec.manager.api;

import me.suwash.swagger.spec.manager.ap.SpecFacade;
import me.suwash.swagger.spec.manager.api.gen.SpecsApi;
import me.suwash.swagger.spec.manager.infra.exception.SpecMgrException;
import me.suwash.swagger.spec.manager.model.gen.Spec;
import me.suwash.swagger.spec.manager.model.gen.Specs;
import io.swagger.annotations.*;
import io.swagger.util.Yaml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.*;
import javax.validation.Valid;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-08-08T09:16:20.502+09:00")

@Controller
public class SpecsApiController implements SpecsApi {

    @Autowired
    private SpecFacade specFacade;

    public ResponseEntity<Void> addSpec(@ApiParam(value = "Specification object that needs to be add" ,required=true )  @Valid @RequestBody Spec body) {
        throw new SpecMgrException("SpecMgr.00002");
    }

    public ResponseEntity<Void> addSpecWithId(@ApiParam(value = "ID of specification that needs to be add",required=true ) @PathVariable("specId") String specId,
        @ApiParam(value = "Specification object that needs to be add" ,required=true )  @Valid @RequestBody Spec body) {
        final Spec added = specFacade.add(specId, body.getPayload());
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    public ResponseEntity<Void> deleteSpecById(@ApiParam(value = "Specification id to delete",required=true ) @PathVariable("specId") String specId) {
        specFacade.delete(specId);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    public ResponseEntity<Object> getSpecById(@ApiParam(value = "ID of spec to return",required=true ) @PathVariable("specId") String specId) {
        final Spec finded = specFacade.findById(specId);
        if (finded == null) {
            return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(finded.getPayload());

    }

    public ResponseEntity<Specs> getSpecs() {
        throw new SpecMgrException("SpecMgr.00002");
    }

    public ResponseEntity<Void> updateSpecWithId(@ApiParam(value = "ID of specification that needs to be update",required=true ) @PathVariable("specId") String specId,
        @ApiParam(value = "Specification object that needs to be update" ,required=true )  @Valid @RequestBody Spec body) {
        throw new SpecMgrException("SpecMgr.00002");
    }

}
