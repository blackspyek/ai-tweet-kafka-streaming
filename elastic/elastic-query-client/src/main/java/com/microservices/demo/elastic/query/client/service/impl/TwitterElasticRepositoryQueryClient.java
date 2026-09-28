package com.microservices.demo.elastic.query.client.service.impl;

import com.microservices.demo.common.util.CollectionsUtil;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.client.exception.ElasticQueryClientException;
import com.microservices.demo.elastic.query.client.repository.TwitterElasticsearchQueryRepository;
import com.microservices.demo.elastic.query.client.service.ElasticQueryClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Primary
public class TwitterElasticRepositoryQueryClient implements ElasticQueryClient<TwitterIndexModel> {
    private final TwitterElasticsearchQueryRepository twitterElasticsearchQueryRepository;

    public TwitterElasticRepositoryQueryClient(TwitterElasticsearchQueryRepository twitterElasticsearchQueryRepository) {
        this.twitterElasticsearchQueryRepository = twitterElasticsearchQueryRepository;
    }

    @Override
    public TwitterIndexModel getIndexModelById(String id) {
        TwitterIndexModel twitterIndexModel = twitterElasticsearchQueryRepository.findById(id)
                .orElseThrow(() ->
                        new ElasticQueryClientException(
                                "No document found at elasticsearch with id " + id
                        )
                );
        return twitterIndexModel;
    }

    @Override
    public List<TwitterIndexModel> getIndexModelByText(String text) {
        return twitterElasticsearchQueryRepository.findByText(text);
    }

    @Override
    public List<TwitterIndexModel> getAllIndexModels() {
        return CollectionsUtil.getInstance().getListFromIterable(twitterElasticsearchQueryRepository.findAll());
    }
}
