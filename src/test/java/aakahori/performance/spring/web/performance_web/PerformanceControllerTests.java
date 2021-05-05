package aakahori.performance.spring.web.performance_web;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import com.google.common.base.Stopwatch;

// 計測用にサーバと別 VM 起動するためコメントアウト (有効にするとサーバも起動してくれる)
// @SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class PerformanceControllerTests {

    @Test
    void test() throws InterruptedException {

        proc("webmvc" , 0, 50, 100, 200, 500);
        proc("webflux", 0, 50, 100, 200, 500, 1000);
    }

    void proc(String type, int... waitList) throws InterruptedException {

        final int COUNT = 10000;
        for (int serverWait : waitList) {

            String path = "/" + type + "?wait=" + serverWait;
            call(path, COUNT);
            Stopwatch watch = Stopwatch.createStarted();
            call(path, COUNT);
            if (serverWait == 0) continue;

            System.out.printf("処理時間 %-7s サーバ %4d ms/回, クライアント %8s/%d回\n",
                    type, serverWait, watch, COUNT);
        }
    }

    void call(String path, int loopCount) throws InterruptedException {

        WebClient web = WebClient.create("http://localhost:8080");
        RequestHeadersSpec<?> request = web.get().uri(path);
        AbstractStringAssert<?> assertResponse = assertThat("Hello World");
        CountDownLatch latch = new CountDownLatch(loopCount);

        for (int i = 0; i < loopCount; i++) {
            request
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(Assertions::fail)
                .doOnTerminate(latch::countDown)
                .subscribe(assertResponse::isEqualTo);
        }
        latch.await(1, TimeUnit.MINUTES);
    }
}
