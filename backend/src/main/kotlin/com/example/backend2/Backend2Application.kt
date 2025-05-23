package com.example.backend2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class Backend2Application

fun main(args: Array<String>) {
    runApplication<Backend2Application>(*args)
}
