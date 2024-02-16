package io.onhigh.os.flygateway.http;

import feign.Client;
import feign.okhttp.OkHttpClient;
import io.onhigh.os.flygateway.GatewayFactory;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

public class OkHttpClientProvider implements HttpClientProvider {


    @Override
    public Client getClient(HttpGatewayFactory.HttpCreateGatewayContext context) {
//        HttpLoggingInterceptor.Level logLevel = environment.getProperty(
//                "flygateway.okhttp.log.level", HttpLoggingInterceptor.Level.class,
//                HttpLoggingInterceptor.Level.BASIC);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(logLevel);

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(context.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(context.getClientReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(context.getClientWriteTimeout(), TimeUnit.MILLISECONDS)
                .build();

        return new OkHttpClient(client);
    }
}
