package com.rallo.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewaySecretRelayFilterTest {

    @Mock
    private GatewayFilterChain chain;

    private GatewaySecretRelayFilter filter;

    @BeforeEach
    void setUp() {
        filter = new GatewaySecretRelayFilter();
    }

    @Test
    void stampsForwardedRequestsWhenSecretConfigured() {
        when(chain.filter(any())).thenReturn(Mono.empty());
        ReflectionTestUtils.setField(filter, "sharedSecret", "internal-secret");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/goals").build());

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> forwarded = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(forwarded.capture());
        assertThat(forwarded.getValue().getRequest().getHeaders()
                .getFirst(GatewaySecretRelayFilter.HEADER)).isEqualTo("internal-secret");
    }

    @Test
    void overwritesClientSuppliedSecretHeader() {
        when(chain.filter(any())).thenReturn(Mono.empty());
        ReflectionTestUtils.setField(filter, "sharedSecret", "internal-secret");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/goals")
                        .header(GatewaySecretRelayFilter.HEADER, "forged")
                        .build());

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> forwarded = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(forwarded.capture());
        assertThat(forwarded.getValue().getRequest().getHeaders()
                .get(GatewaySecretRelayFilter.HEADER)).containsExactly("internal-secret");
    }

    @Test
    void leavesRequestUntouchedWhenSecretNotConfigured() {
        when(chain.filter(any())).thenReturn(Mono.empty());
        ReflectionTestUtils.setField(filter, "sharedSecret", "");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/goals").build());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        assertThat(exchange.getRequest().getHeaders().containsKey(GatewaySecretRelayFilter.HEADER))
                .isFalse();
    }

    @Test
    void runsBeforeJwtFilter() {
        assertThat(filter.getOrder()).isLessThan(-1);
    }
}
