package io.onhigh.os.flygateway.http;

import io.onhigh.os.flygateway.FlyEnvironment;

public interface HttpEnvironment extends FlyEnvironment {

    HttpClientProvider getHttpClientProvider();

}
