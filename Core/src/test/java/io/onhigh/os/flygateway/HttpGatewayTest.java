package io.onhigh.os.flygateway;

import io.onhigh.os.flygateway.dto.CharactersResponse;
import io.onhigh.os.flygateway.http.HttpClientProvider;
import io.onhigh.os.flygateway.http.HttpEnvironment;
import io.onhigh.os.flygateway.http.OkHttpClientProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class HttpGatewayTest {

    private static final HttpEnvironment DEFAULT_ENV = new HttpEnvironment() {

        @Override
        public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
            return null;
        }

        @Override
        public Map<String, String> getPropertiesMap() {
            return null;
        }

        @Override
        public HttpClientProvider getHttpClientProvider() {
            return new OkHttpClientProvider();
        }
    };

    @Test
    public void defaultTest() {
        TestHttpGateway testHttpGateway = FlyGateway.get(TestHttpGateway.class, DEFAULT_ENV);
        CharactersResponse charactersResponse = testHttpGateway.getCharacters();
        Assertions.assertFalse(charactersResponse.getResults().isEmpty());
        Assertions.assertEquals(20, charactersResponse.getResults().size());
    }

}
