package panda.vendor.management.repository;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;
import panda.vendor.management.entities.Vendor;

@Configuration
public class IdInitializer {
	
	public void ensureId(Vendor vendor) {
        if (vendor.getVendorId() == null || vendor.getVendorId().isEmpty()) {
        	vendor.setVendorId("Pan-ven-"+UUID.randomUUID().toString());
        }
    }


}
