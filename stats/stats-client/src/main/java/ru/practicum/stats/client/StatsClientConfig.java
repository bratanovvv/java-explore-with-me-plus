package ru.practicum.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Конфигурация HTTP-клиента сервиса статистики.
 * <p>
 * Бин попадает в контекст приложения при компонентном сканировании пакета
 * {@code ru.practicum.stats.client}.
 */
@Configuration(proxyBeanMethods = false)
public class StatsClientConfig {

    @Bean
    public RestClient statsRestClient(@Value("${stats-server.url:http://localhost:9090}") String serverUrl) {
        return RestClient.builder()
                .baseUrl(serverUrl)
                .build();
    }

    @Bean
    public StatsClient statsClient(RestClient statsRestClient) {
        return new StatsClientImpl(statsRestClient);
    }
}
