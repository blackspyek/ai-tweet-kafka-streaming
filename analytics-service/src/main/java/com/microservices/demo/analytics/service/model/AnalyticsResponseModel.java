package com.microservices.demo.analytics.service.model;

import com.microservices.demo.analytics.service.dataaccess.entity.AnalyticsEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsResponseModel {
    private UUID id;
    private String word;
    private long wordCount;
}
