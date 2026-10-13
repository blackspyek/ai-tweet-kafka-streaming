package com.microservices.demo.elastic.query.service;

import com.microservices.demo.elastic.query.model.ElasticQueryServiceAnalyticsResponseModel;
import com.microservices.demo.elastic.query.model.ElasticQueryServiceResponseModel;

import java.util.List;

public interface ElasticQueryService {
    ElasticQueryServiceResponseModel getDocumentById(String id);
    ElasticQueryServiceAnalyticsResponseModel getDocumentsByText(String text, String accessToken);
    List<ElasticQueryServiceResponseModel> getAllDocuments();
}
