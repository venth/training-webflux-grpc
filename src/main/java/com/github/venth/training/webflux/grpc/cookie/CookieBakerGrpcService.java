package com.github.venth.training.webflux.grpc.cookie;

import com.github.venth.training.webflux.grpc.api.cookie.BakeRequest;
import com.github.venth.training.webflux.grpc.api.cookie.Cookie;
import com.github.venth.training.webflux.grpc.api.cookie.RxCookieBakerGrpc;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.RequiredArgsConstructor;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CookieBakerGrpcService extends RxCookieBakerGrpc.CookieBakerImplBase {

    private final CookieBaker cookieBaker;

    @Override
    public Flowable<Cookie> bake(Single<BakeRequest> request) {
        return request.flatMapPublisher(bakeRequest -> cookieBaker.bake(bakeRequest.getNumberOfCookies()));
    }
}
