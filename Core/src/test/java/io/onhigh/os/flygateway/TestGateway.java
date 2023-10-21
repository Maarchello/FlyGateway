package io.onhigh.os.flygateway;

import io.onhigh.os.flygateway.dto.CharactersResponse;
import io.onhigh.os.flygateway.http.api.GetRequest;
import io.onhigh.os.flygateway.http.api.HttpGateway;

@HttpGateway(url = "https://rickandmortyapi.com/api")
public interface TestGateway {

    @GetRequest("/character")
    CharactersResponse getCharacters();

}
