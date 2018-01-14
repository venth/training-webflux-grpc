package com.github.venth.training.webflux.grpc.cookie;

import com.github.venth.training.webflux.grpc.api.cookie.Cookie;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

@Component
class CookieBaker {

    public Flowable<Cookie> bake(long numberOfCookiesToBake) {
        return Flowable.empty();
    }

}
