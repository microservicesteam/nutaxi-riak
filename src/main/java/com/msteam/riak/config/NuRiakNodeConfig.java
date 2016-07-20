package com.msteam.riak.config;

import com.basho.riak.client.core.RiakNode;
import com.basho.riak.client.core.query.Location;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NuRiakNodeConfig {

    @Value("riak.node.minConnections")
    private int riakNodeMinConnections;

    @Value("riak.node.maxConnections")
    private int riakNodeMaxConnections;

    @Bean
    public RiakNode.Builder getRiakNodeBuilder() {
        RiakNode.Builder builder = new RiakNode.Builder();
        builder.withMinConnections(riakNodeMinConnections);
        builder.withMaxConnections(riakNodeMaxConnections);
        return builder;
    }
}
