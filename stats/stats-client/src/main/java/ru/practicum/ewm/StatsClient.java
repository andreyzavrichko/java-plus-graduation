package ru.practicum.ewm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Component
public class StatsClient {

    private final RestTemplate rest;
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final String statsServiceId;

    public StatsClient(
            DiscoveryClient discoveryClient,
            @Value("${ewm.stats-server.id:stats-server}") String statsServiceId
    ) {
        this.discoveryClient = discoveryClient;
        this.statsServiceId = statsServiceId;
        this.rest = new RestTemplate();
        this.retryTemplate = buildRetryTemplate();
    }

    public void createHit(CreateHitDto createDto) {
        URI uri = makeUri("/hit");
        HttpEntity<CreateHitDto> requestEntity = new HttpEntity<>(createDto, defaultHeaders());
        rest.exchange(uri, HttpMethod.POST, requestEntity, HitDto.class);
    }

    public ResponseEntity<List<ViewStatsDto>> getStats(
            String start,
            String end,
            List<String> uris,
            Boolean unique
    ) {
        URI uri = UriComponentsBuilder
                .fromUri(makeUri("/stats"))
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris != null ? uris.toArray() : new String[0])
                .queryParam("unique", unique)
                .build()
                .toUri();

        HttpEntity<Void> requestEntity = new HttpEntity<>(defaultHeaders());

        return rest.exchange(
                uri,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {
                });
    }

    private URI makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(ctx -> getInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    private ServiceInstance getInstance() {
        List<ServiceInstance> instances = discoveryClient.getInstances(statsServiceId);
        if (instances == null || instances.isEmpty()) {
            throw new StatsServerUnavailableException(
                    "Нет доступных экземпляров сервиса статистики с id: " + statsServiceId
            );
        }
        return instances.getFirst();
    }

    private RetryTemplate buildRetryTemplate() {
        RetryTemplate template = new RetryTemplate();

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(3000L);
        template.setBackOffPolicy(backOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        template.setRetryPolicy(retryPolicy);

        return template;
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}