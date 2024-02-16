package io.onhigh.os.flygateway;

import io.onhigh.os.flygateway.util.VarArgs;

public interface FlyErrorDecoder<O> {

    O decode(VarArgs varArgs);

}
