package com.microservice.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;


// se esta añadiendo eso ya que me permite habilitar las configuraciones del "configserver"
@EnableConfigServer
//----------------------------------------------------------------------------------------
@SpringBootApplication
public class MicroserviceConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroserviceConfigApplication.class, args);
	}

}
