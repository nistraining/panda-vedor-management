package panda.vendor.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PandaVendorManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(PandaVendorManagementApplication.class, args);
	}

}
