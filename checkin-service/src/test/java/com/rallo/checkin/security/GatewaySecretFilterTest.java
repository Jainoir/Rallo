package com.rallo.checkin.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class GatewaySecretFilterTest {

    private GatewaySecretFilter filter;

    @BeforeEach
    void setUp() {
        filter = new GatewaySecretFilter();
        ReflectionTestUtils.setField(filter, "sharedSecret", "internal-secret");
    }

    @Test
    void rejectsRequestWithoutSecret() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/goals");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void rejectsRequestWithWrongSecret() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/goals");
        request.addHeader(GatewaySecretFilter.HEADER, "forged");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void allowsRequestStampedByGateway() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/goals");
        request.addHeader(GatewaySecretFilter.HEADER, "internal-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void swaggerAndActuatorStayReachableWithoutSecret() throws ServletException, IOException {
        for (String path : new String[]{"/swagger-ui/index.html", "/api-docs", "/actuator/health"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
            request.setRequestURI(path);
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).as(path).isEqualTo(200);
            assertThat(chain.getRequest()).as(path).isNotNull();
        }
    }

    @Test
    void disabledWhenNoSecretConfigured() throws ServletException, IOException {
        ReflectionTestUtils.setField(filter, "sharedSecret", "");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/goals");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }
}
