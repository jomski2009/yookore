package com.yookos.yookore.config;

import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.RestGraphDatabase;
import org.neo4j.rest.graphdb.batch.BatchRestAPI;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by jome on 2014/08/29.
 */

@Configuration
public class Neo4jConfig {
    @Autowired
    Environment env;

    @Bean
    @Autowired
    RestGraphDatabase restGraphDatabase() {
        return new RestGraphDatabase(env.getProperty("neo4j.db",
                "http://localhost:7474/db/data/"));
    }

    @Bean
    @Autowired
    RestAPIFacade restAPIFacade() {
        return new RestAPIFacade(env.getProperty("neo4j.db",
                "http://localhost:7474/db/data/"));
    }

    @Bean
    RestCypherQueryEngine restCypherQueryEngine() {
        RestCypherQueryEngine engine = new RestCypherQueryEngine(restAPIFacade());

        return engine;
    }

    @Bean
    BatchRestAPI batchRestAPI() {
        return new BatchRestAPI(env.getProperty("neo4j.db",
                "http://localhost:7474/db/data/"), restAPIFacade());
    }
}
