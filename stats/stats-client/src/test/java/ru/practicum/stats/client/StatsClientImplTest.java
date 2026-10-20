package ru.practicum.stats.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.RestClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class StatsClientImplTest {

    private static final String SERVER_URL = "http://localhost:9090";
    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 20, 0, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 10, 21, 0, 0, 0);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private MockRestServiceServer server;
    private StatsClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(SERVER_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new StatsClientImpl(builder.build());
    }

    @Test
    void hit_shouldPostToHitEndpoint() {
        server.expect(requestTo(SERVER_URL + "/hit"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        client.hit(new EndpointHitDto(null, "ewm-main-service", "/events/1", "1.1.1.1", START));

        server.verify();
    }

    @Test
    void getStats_shouldParseResponse() throws Exception {
        List<ViewStatsDto> response = List.of(new ViewStatsDto("ewm-main-service", "/events/1", 5L));

        expectGetStats()
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        List<ViewStatsDto> stats = client.getStats(START, END, List.of("/events/1"), true);

        assertThat(stats).isEqualTo(response);
        server.verify();
    }

    @Test
    void getStats_whenServerError_shouldReturnEmptyList() {
        expectGetStats()
                .andRespond(withServerError());

        List<ViewStatsDto> stats = client.getStats(START, END, null, false);

        assertThat(stats).isEmpty();
        server.verify();
    }

    private ResponseActions expectGetStats() {
        return server.expect(requestTo(startsWith(SERVER_URL + "/stats")))
                .andExpect(method(HttpMethod.GET));
    }
}
