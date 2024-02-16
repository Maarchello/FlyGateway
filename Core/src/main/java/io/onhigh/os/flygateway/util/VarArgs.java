package io.onhigh.os.flygateway.util;

import lombok.Getter;

@Getter
public class VarArgs {

    private final Object[] objects;

    public VarArgs(Object... objects) {
        this.objects = objects;
    }
}
