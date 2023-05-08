package net.frey.mongo.config;

import static java.util.Collections.singletonList;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.lang.NonNull;

@Configuration
public class MongoConfig extends AbstractReactiveMongoConfiguration {
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create();
    }

    @NonNull
    @Override
    protected String getDatabaseName() {
        return "beer";
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        builder.credential(MongoCredential.createCredential("root", "admin", "example".toCharArray()))
                .applyToClusterSettings(
                        settings -> settings.hosts((singletonList(new ServerAddress("127.0.0.1", 27017)))));
    }
}
