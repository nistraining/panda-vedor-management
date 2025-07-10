package panda.vendor.management.config;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import panda.vendor.management.services.VendorLogService;

@Component
public class SchemaRegistryUtils {
	
	@Autowired
	private VendorLogService logService;
	
	 public boolean isSchemaRegistryAvailable() {
	        try {
	            URL url = new URL("http://localhost:8081/subjects");
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            connection.setRequestMethod("GET");
	            connection.setConnectTimeout(3000);
	            connection.connect();
	            return connection.getResponseCode() == 200;
	        } catch (IOException e) {
	            System.out.println("Schema Registry not reachable: " + e.getMessage());
	            return false;
	        }
	    }

	    public void waitForSchemaRegistry() {
	        int maxRetries = 5;
	        int delayMs = 2000;

	        for (int i = 0; i < maxRetries; i++) {
	            if (isSchemaRegistryAvailable()) {
	                System.out.println("✅ Schema Registry is available.");
	                return;
	            }
	            System.out.println("⏳ Retry " + (i + 1) + ": Waiting for Schema Registry...");
	            try {
	                Thread.sleep(delayMs);
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	                logService.logMessageToCloudWatch("Interrupted while waiting for Schema Registry");
	                System.out.println("Interrupted while waiting for Schema Registry.");
	                return;
	            }
	        }
	        logService.logMessageToCloudWatch("❌ Schema Registry not available after retries.");
	        System.out.println("❌ Schema Registry not available after retries.");
	    }
	}

