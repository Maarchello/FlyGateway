package io.onhigh.os.flygateway;

import io.onhigh.os.flygateway.dto.CharactersResponse;
import io.onhigh.os.flygateway.http.HttpClientProvider;
import io.onhigh.os.flygateway.http.OkHttpClientProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class FlyGatewayTest {

    private static final FlyEnvironment DEFAULT_ENV = new FlyEnvironment() {

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
        TestGateway testGateway = FlyGateway.get(TestGateway.class, DEFAULT_ENV);
        CharactersResponse charactersResponse = testGateway.getCharacters();
        Assertions.assertFalse(charactersResponse.getResults().isEmpty());

    }

}
