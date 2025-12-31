package com.event.event_reservation_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class EventReservationSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventReservationSystemApplication.class, args);
    }
}
