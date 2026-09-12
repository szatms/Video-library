package io.github.szatms.videolibrary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class VideolibraryApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideolibraryApplication.class, args);
	}

}
