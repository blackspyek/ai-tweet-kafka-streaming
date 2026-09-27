package com.microservices.demo.elastic.query.client.service.impl;

import com.microservices.demo.config.ElasticConfigData;
import com.microservices.demo.config.ElasticQueryConfigData;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.client.exception.ElasticQueryClientException;
import com.microservices.demo.elastic.query.client.service.ElasticQueryClient;
import com.microservices.demo.elastic.query.client.util.ElasticQueryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TwitterElasticQueryClient implements ElasticQueryClient<TwitterIndexModel> {
    private static final Logger log = LoggerFactory.getLogger(TwitterElasticQueryClient.class);
    private final ElasticConfigData elasticConfigData;
    private final ElasticQueryConfigData elasticQueryConfigData;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticQueryUtil<TwitterIndexModel> elasticQueryUtil;

    public TwitterElasticQueryClient(ElasticConfigData elasticConfigData, ElasticQueryConfigData elasticQueryConfigData, ElasticsearchOperations elasticsearchOperations, ElasticQueryUtil<TwitterIndexModel> elasticQueryUtil) {
        this.elasticConfigData = elasticConfigData;
        this.elasticQueryConfigData = elasticQueryConfigData;
        this.elasticsearchOperations = elasticsearchOperations;
        this.elasticQueryUtil = elasticQueryUtil;
    }


    @Override
    public TwitterIndexModel getIndexModelById(String id) {
        Query searchQueryById = elasticQueryUtil.getSearchQueryById(id);
        SearchHit<TwitterIndexModel> searchResult = elasticsearchOperations.searchOne(searchQueryById, TwitterIndexModel.class, IndexCoordinates.of(elasticConfigData.getIndexName()));
        if (searchResult != null) {
            throw new ElasticQueryClientException("No document found at elasticsearch with id " + id);
        }
        return searchResult.getContent();

    }

    @Override
    public List<TwitterIndexModel> getIndexModelByText(String text) {
        Query query = elasticQueryUtil.getSearchQueryByFieldText(elasticQueryConfigData.getTextField(), text);
        SearchHits<TwitterIndexModel> search = elasticsearchOperations.search(query, TwitterIndexModel.class, IndexCoordinates.of(elasticConfigData.getIndexName()));
        return search.get().map(SearchHit::getContent).collect(Collectors.toList());
    }

    @Override
    public List<TwitterIndexModel> getAllIndexModels() {
        Query searchQueryForAll = elasticQueryUtil.getSearchQueryForAll();
        SearchHits<TwitterIndexModel> search = elasticsearchOperations.search(searchQueryForAll, TwitterIndexModel.class, IndexCoordinates.of(elasticConfigData.getIndexName()));
        return search.get().map(SearchHit::getContent).collect(Collectors.toList());
    }
}
