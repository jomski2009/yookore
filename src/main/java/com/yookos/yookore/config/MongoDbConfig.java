package com.yookos.yookore.config;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.UnknownHostException;

/**
 * Created by jome on 2014/08/27.
 */

@Configuration
public class MongoDbConfig {
    @Autowired
    Environment environment;

    @Bean
    MongoClient mongoClient(){
        try {
            return new MongoClient(environment.getProperty("mongo.db.host", "localhost"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    DB yookosdb(){
        DB yookosdb = mongoClient().getDB("yookosreco");
        return yookosdb;
    }

    @Bean
    Morphia morphia(){
        Morphia morphia = new Morphia();
        morphia.mapPackage("com.yookos");

        return morphia;
    }

    @Bean
    Datastore datastore(){
        AdvancedDatastore ads = (AdvancedDatastore) morphia().createDatastore(mongoClient(),
                environment.getProperty("mongo.db.database", "yookosreco"));
        ads.ensureIndexes();

        return ads;
    }

}
