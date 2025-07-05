package panda.vendor.management.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import panda.vendor.management.entities.Vendor;
import panda.vendor.management.services.VendorServices;

@RestController
@RequestMapping("/vendor")
public class VendorController {
	
    @Autowired
	private VendorServices vendorService;

	
	@RequestMapping("/save")
	public ResponseEntity<Vendor> saveVendors(@RequestBody Vendor vendor){
	Vendor vendors=	vendorService.saveVendor(vendor);
	return new ResponseEntity<Vendor>(vendors,HttpStatus.CREATED);
	}
	
  
	@GetMapping("/getAllVendors")
	public ResponseEntity<List<Vendor>> getAllVendors() {
	    List<Vendor> vendors = vendorService.getAllVendors();
	    return new ResponseEntity<>(vendors, HttpStatus.OK);
	}
}
