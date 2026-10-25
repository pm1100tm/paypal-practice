package com.swd.shopswd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods
public class ShopSwdApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopSwdApplication.class, args);
    }
}
