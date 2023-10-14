package io.onhigh.os.flygateway.http.codec;

import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.StringDecoder;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author Marat Kadzhaev
 * @since 22 февр. 2022
 */
@RequiredArgsConstructor
public class JacksonExtendedDecoder implements Decoder {

    private final Decoder delegate;
    private final StringDecoder stringDecoder = new StringDecoder();

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        if (type == String.class) {
            return stringDecoder.decode(response, type);
        }

        return delegate.decode(response, type);
    }
}
