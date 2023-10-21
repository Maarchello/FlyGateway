package io.onhigh.os.flygateway;

import feign.RequestLine;
import io.onhigh.os.flygateway.http.api.GetRequest;
import io.onhigh.os.flygateway.http.api.HttpGateway;

import java.util.List;

@HttpGateway(url = "https://rickandmortyapi.com/api")
public interface TestGateway {

    @GetRequest("/character")
    List<Object> getCharacters();

}
