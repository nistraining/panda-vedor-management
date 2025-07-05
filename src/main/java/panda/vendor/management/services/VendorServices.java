package panda.vendor.management.services;

import java.util.List;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import panda.vendor.management.entities.Vendor;
import panda.vendor.management.repository.VendorRepository;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

@Service
@Slf4j
public class VendorServices {
	
	private static final Logger log = LoggerFactory.getLogger(VendorServices.class);

	@Autowired
	private VendorRepository vendorRepo;
	
	public Vendor saveVendor(Vendor vendor) {
		try {
			return vendorRepo.saveVendor(vendor);
		}catch(DynamoDbException e) {
			log.info("Error occurred while saving a vendor :",e.getMessage());
		}
		return vendor;
	}
	
	public List<Vendor> getAllVendors(){
		return vendorRepo.findAllVendors();
	}
	
	

}
