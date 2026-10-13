package com.microservices.demo.elastic.query.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElasticQueryServiceAnalyticsResponseModel {
    private List<ElasticQueryServiceResponseModel> queryResponseModels;
    private Long wordCount;
}
