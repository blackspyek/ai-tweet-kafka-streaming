package com.microservices.demo.kafka.streams.service.controller;

import com.microservices.demo.kafka.streams.service.model.KafkaStreamsResponseModel;
import com.microservices.demo.kafka.streams.service.runner.StreamsRunner;
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

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping(value = "/")
@Slf4j
public class KafkaStreamsController {
    private final StreamsRunner<String, Long> kafkaStreamsRunner;

    public KafkaStreamsController(StreamsRunner<String, Long> streamsRunner) {
        this.kafkaStreamsRunner = streamsRunner;
    }
    @GetMapping("get-word-count-by-word/{word}")
    @Operation(summary = "Get word count by word.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successs.", content = {
                    @Content(schema = @Schema(implementation = KafkaStreamsResponseModel.class))
            }),
            @ApiResponse(responseCode = "400", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Unexpected Error"),
    })

    public ResponseEntity<KafkaStreamsResponseModel> getWordCountByWord(@PathVariable @NotEmpty String word){
        Long wordCount = kafkaStreamsRunner.getValueByKey(word);
        log.info("Get word count by word: {}", wordCount);
        return ResponseEntity.ok(
                KafkaStreamsResponseModel.builder()
                        .word(word)
                        .wordCount(wordCount)
                        .build()
        );
    }
}
