package com.example.demo.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@RestController
public class TestEndpointController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEndpointController.class);
    // NO THREAD SAFE IMPLEMENTATION!!!
    private static long start_time;

    private final Gauge myDuration;

    //tps
    public static int TICK_COUNT = 0;
    private final Gauge myTps;


    public TestEndpointController(MeterRegistry registry) {

        this.myDuration = Gauge.builder("myDuration", this, TestEndpointController::calcDuration)
                .baseUnit("seconds").tag("env", "dev").description("This is the time since last-call").register(registry);
        this.myTps = Gauge
                .builder("myTps", this, TestEndpointController::sumToTps).baseUnit("tps")
                .tag("env", "dev").description("This is Tps").register(registry);

    }

    @Timed
    @GetMapping("/")
    ResponseEntity<?> getAccessToHttpsApp() {
        LOGGER.info("API test!");


        //처음 트랜잭션 발생시간
        if(TICK_COUNT==0){
            start_time = System.currentTimeMillis();
        }

        //트랜잭션 횟수 증가
        TICK_COUNT +=1;


        this.myDuration.value();
        this.myTps.value();

        return new ResponseEntity<>("test_http", HttpStatus.OK);
    }

    //duration(처음 트랜잭션 발생후 actuator/prometheus 호출 전까지의 시간
    public Double calcDuration() {
        return (System.currentTimeMillis()-start_time) / 1000d;
    }

    // 트랜잭션/duration 반환
    public Double sumToTps() {
        return TICK_COUNT/(calcDuration());
    }


}
