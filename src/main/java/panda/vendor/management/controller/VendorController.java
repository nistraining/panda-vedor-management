package panda.vendor.management.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import panda.vendor.management.dto.VendorBatchResponseDTO;
import panda.vendor.management.entities.Vendor;
import panda.vendor.management.services.VendorLogService;
import panda.vendor.management.services.VendorServices;

@RestController
@RequestMapping("/vendor")
public class VendorController {
	
    @Autowired
	private VendorServices vendorService;
    
    @Autowired
    private VendorLogService logService;
	
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
	
	@PostMapping("/upload")
    public ResponseEntity<?> uploadVendorCSV(@RequestParam("file") MultipartFile file) {
        try {
            List<Vendor> parsedVendors = vendorService.parseCSV(file);

            if (parsedVendors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body("The uploaded file contains no vendor records.");
            }

            VendorBatchResponseDTO response = vendorService.saveAllVendors(parsedVendors);

            Map<String, Object> result = new HashMap<>();
            result.put("savedVendorIds", response.getSavedVendorIds());
            result.put("skippedDuplicates", response.getSkippedDuplicates());
            result.put("totalProcessed", parsedVendors.size());

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Failed to process CSV file: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Unexpected error: " + ex.getMessage());
        }
    }
	
	
	@GetMapping("/vendors/resolve")
	public ResponseEntity<?> resolveVendor(@RequestParam String vendorName, @RequestParam int location) {
	   System.out.println("Inside the controller");
		
		try {
	        Optional<Vendor> resolved = vendorService.resolveVendorByNameAndLocation(vendorName, location);

	        if (resolved.isPresent()) {
	            return ResponseEntity.ok(resolved.get());
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body("Vendor not found for name: " + vendorName + " and location: " + location);
	        }

	    } catch (Exception ex) {
	    	logService.logMessageToCloudWatch("Error resolving vendor :Name :"+ vendorName+" Location:" +location +"Reason :" +ex.getMessage());
	        
	    	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body("Internal error occurred while resolving vendor.");
	    }
	}
	
	
	

}
