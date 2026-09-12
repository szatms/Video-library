package io.github.szatms.videolibrary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        //return RestClient.create("http://127.0.0.1:8000");
        return RestClient.create("http://youtube-harvester:8000");
    }
}
