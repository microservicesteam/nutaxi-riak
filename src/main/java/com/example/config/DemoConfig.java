package com.example.config;

import com.example.DemoApplication;
import com.msteam.aws.config.NuServerConfig;
import com.msteam.riak.config.NuRiakNodeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DemoConfig {

    @Autowired
    private NuServerConfig nuServerConfig;

    @Autowired
    private NuRiakNodeConfig nuRiakNodeConfig;

    @Bean
    public DemoApplication getDemoApplication() {
        return new DemoApplication(nuRiakNodeConfig.getRiakCluster(),
                nuRiakNodeConfig.getRiakClient(), nuServerConfig.getNuAwsServerArray());
    }
}
