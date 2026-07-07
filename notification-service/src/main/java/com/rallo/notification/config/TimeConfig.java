package com.rallo.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfig {

    /** Injectable clock so time-dependent logic is testable with a fixed clock. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
