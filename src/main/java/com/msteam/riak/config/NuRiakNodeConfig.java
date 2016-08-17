package com.msteam.riak.config;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import com.msteam.aws.config.NuServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.basho.riak.client.core.RiakNode.Builder.buildNodes;

@Configuration
public class NuRiakNodeConfig {

    @Autowired
    private NuServerConfig nuServerConfig;

    @Value("riak.node.minConnections")
    private int riakNodeMinConnections;

    @Value("riak.node.maxConnections")
    private int riakNodeMaxConnections;

    @Bean
    private RiakNode.Builder getRiakNodeBuilder() {
        RiakNode.Builder builder = new RiakNode.Builder();
        builder.withMinConnections(riakNodeMinConnections);
        builder.withMaxConnections(riakNodeMaxConnections);
        return builder;
    }

    @Bean
    public RiakCluster getRiakCluster() {
        List<RiakNode> nodes = buildNodes(getRiakNodeBuilder(), nuServerConfig.getNuAwsServerArray().discoverPublicIps());
        return new RiakCluster.Builder(nodes).build();
    }

    @Bean
    public RiakClient getRiakClient() {
        return new RiakClient(getRiakCluster());
    }
}
