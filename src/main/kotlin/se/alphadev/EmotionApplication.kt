package se.alphadev

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class EmotionApplication

    fun main(args: Array<String>) {
        SpringApplication.run(EmotionApplication::class.java, *args)
    }
