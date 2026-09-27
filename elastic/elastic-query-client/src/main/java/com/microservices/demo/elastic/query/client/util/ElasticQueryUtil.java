package com.microservices.demo.elastic.query.client.util;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.microservices.demo.elastic.model.index.IndexModel;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class ElasticQueryUtil<T extends IndexModel> {
    public Query getSearchQueryById(String id) {
        return NativeQuery.builder()
                .withIds(Collections.singleton(id))
                .build();
    }
    public Query getSearchQueryByFieldText(String field, String text) {
        return NativeQuery.builder()
                .withQuery(q -> q
                        .match(m -> m
                                .field(field)
                                .query(text)
                        )
                ).build();
    }
    public Query getSearchQueryForAll(){
        return NativeQuery.builder()
                .withQuery(q -> q
                        .matchAll(m -> m))
                .build();
    }
}
