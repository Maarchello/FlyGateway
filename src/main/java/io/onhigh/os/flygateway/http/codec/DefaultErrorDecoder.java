package io.onhigh.os.flygateway.http.codec;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class DefaultErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        FeignException exception = feign.FeignException.errorStatus(methodKey, response);
        return null;
    }

}
