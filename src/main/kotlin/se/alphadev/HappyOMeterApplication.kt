package se.alphadev

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class HappyOMeterApplication

fun main(args: Array<String>) {
    SpringApplication.run(HappyOMeterApplication::class.java, *args)
}
