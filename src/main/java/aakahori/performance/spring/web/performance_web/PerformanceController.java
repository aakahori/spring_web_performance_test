package aakahori.performance.spring.web.performance_web;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
public class PerformanceController {

    public static void main(String[] args) {
        SpringApplication.run(PerformanceController.class, args);
    }

    @GetMapping("/webmvc")
    public String webmvc(long wait) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(wait);
        return "Hello World";
    }

    @GetMapping("/webflux")
    public Mono<String> webflux(long wait) {
        return Mono.just("Hello World").delayElement(Duration.ofMillis(wait));
    }
}
