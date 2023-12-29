package it.unitn.APCM.ACME.Guard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;

@SpringBootApplication
public class Guard_RESTApp {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Guard_RESTApp.class);
		HashMap<String, Object> properties = new HashMap<String, Object>(2);
		properties.put("server.port", "8090");
		properties.put("spring.application.name", "Guard_RESTApp");
		app.setDefaultProperties(properties);
		app.run(args);
	}
}