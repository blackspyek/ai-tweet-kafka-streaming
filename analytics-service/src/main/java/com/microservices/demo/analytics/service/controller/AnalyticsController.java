package com.microservices.demo.analytics.service.controller;

import com.microservices.demo.analytics.service.model.AnalyticsResponseModel;
import com.microservices.demo.analytics.service.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping(value = "/")
@Slf4j
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
    @GetMapping("/get-word-count-by-word/{word}")
    @Operation(summary = "Get analytics by word.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Success.",
                    content = {
                            @Content(
                                    mediaType = "application/vnd.api.v1+json",
                                    schema = @Schema(
                                            implementation = AnalyticsResponseModel.class
                                    )
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Not found."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected error."
            )
    })
    public @ResponseBody
    ResponseEntity<AnalyticsResponseModel> getWordCountByWord(
            @PathVariable @NotEmpty String word
    ) {
        Optional<AnalyticsResponseModel> response =
                analyticsService.getWordAnalytics(word);

        if (response.isPresent()) {
            log.info(
                    "Analytics data returned with id {}",
                    response.get().getId()
            );

            return ResponseEntity.ok(response.get());
        }

        return ResponseEntity.ok(
                AnalyticsResponseModel.builder().build()
        );
    }

}
