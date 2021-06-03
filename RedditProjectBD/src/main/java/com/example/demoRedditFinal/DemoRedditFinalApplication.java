package com.example.demoRedditFinal;

import com.example.demoRedditFinal.config.SwaggerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@Import(SwaggerConfiguration.class)
public class DemoRedditFinalApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoRedditFinalApplication.class, args);
	}

}
