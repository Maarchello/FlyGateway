package io.onhigh.os.flygateway.http;

import feign.Client;
import io.onhigh.os.flygateway.GatewayFactory;

@FunctionalInterface
public interface HttpClientProvider {

    Client getClient(GatewayFactory.Context context);

}
