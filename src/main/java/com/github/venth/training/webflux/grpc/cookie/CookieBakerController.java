package com.github.venth.training.webflux.grpc.cookie;

import com.github.venth.training.webflux.grpc.api.cookie.Cookie;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class CookieBakerController {

    static final long COOKIES_LIMIT = 10;

    private final CookieBaker cookieBaker;

    @GetMapping(path = "/bake", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    Flowable<Cookie> bake() {
        return cookieBaker.bake(COOKIES_LIMIT);
    }
}
