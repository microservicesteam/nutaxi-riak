package com.msteam.riak.config;

import com.amazonaws.services.ec2.AmazonEC2AsyncClient;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Filter;
import com.basho.riak.client.core.RiakNode;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NuRiakClusterConfig {

    @Value("aws.tag.key.type")
    private String instanceTagKeyType;

    @Value("aws.tag.value.riak")
    private String instanceTagValueRiak;

    @Bean
    public AmazonEC2Client getAmazonEC2Client() {
        //TODO LM: Use credentials provider here instead of Java System Properties - aws.accessKeyId and aws.secretKey
        return new AmazonEC2AsyncClient();
    }

    @Bean
    public Filter getInstanceFilterForRiak() {
        //TODO LM: Add docs to git. This will filter by tag on aws. Currently my riak instances are tagged as "type = riak"
        return new Filter(instanceTagKeyType, ImmutableList.of(instanceTagValueRiak));
    }

}
