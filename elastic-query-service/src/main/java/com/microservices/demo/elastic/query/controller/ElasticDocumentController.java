package com.microservices.demo.elastic.query.controller;

import com.microservices.demo.elastic.query.model.ElasticQueryServiceRequestModel;
import com.microservices.demo.elastic.query.model.ElasticQueryServiceResponseModel;
import com.microservices.demo.elastic.query.service.ElasticQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping(value = "/documents")
public class ElasticDocumentController {
    private static final Logger log =  LoggerFactory.getLogger(ElasticDocumentController.class);
    private final ElasticQueryService elasticQueryService;

    @Value("${server.port}")
    private String port;
    public ElasticDocumentController(ElasticQueryService elasticQueryService) {
        this.elasticQueryService = elasticQueryService;
    }

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    @Operation(summary = "Get all elastic documents!")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(schema = @Schema(implementation = ElasticQueryServiceResponseModel.class))
            }),
            @ApiResponse(responseCode = "400", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error!")
    })
    @GetMapping("/")
    public ResponseEntity<List<ElasticQueryServiceResponseModel>> getAllDocuments(){
        return ResponseEntity.ok().body(elasticQueryService.getAllDocuments());
    }

    @PreAuthorize("hasPermission(#id, 'ElasticQueryServiceResponseModel', 'READ')")
    @Operation(summary = "Get elastic document by id!")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(schema = @Schema(implementation = ElasticQueryServiceResponseModel.class))
            }),
            @ApiResponse(responseCode = "400", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error!")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ElasticQueryServiceResponseModel> getDocumentById(@PathVariable @NotEmpty String id){
        ElasticQueryServiceResponseModel elasticQueryServiceResponseModel = elasticQueryService.getDocumentById(id);
        return ResponseEntity.ok().body(elasticQueryServiceResponseModel);
    }

    @Operation(summary = "Get elastic document by text!")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(schema = @Schema(implementation = ElasticQueryServiceResponseModel.class))
            }),
            @ApiResponse(responseCode = "400", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error!")
    })

    @PreAuthorize("hasRole('APP_USER_ROLE') || hasRole('APP_SUPER_USER_ROLE') ||hasAuthority('SCOPE_APP_USER_ROLE')")
    @PostAuthorize("hasPermission(returnObject, 'READ')")
    @PostMapping("/get-document-by-text")
    public ResponseEntity<List<ElasticQueryServiceResponseModel>> getDocumentByText(@RequestBody @Valid ElasticQueryServiceRequestModel elasticQueryServiceRequestModel){
        log.debug("Elasticsearch  returned {} of documents on port {}", "XD", port);
        return ResponseEntity.ok().body(elasticQueryService.getDocumentsByText(elasticQueryServiceRequestModel.getText()));
    }
}
