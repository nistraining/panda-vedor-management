package panda.vendor.management.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import panda.vendor.management.dto.VendorBatchResponseDTO;
import panda.vendor.management.entities.Vendor;
import panda.vendor.management.exceptions.VendorNotFoundException;
import panda.vendor.management.repository.VendorRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

@Service
@Slf4j
public class VendorServices {
	
	@Autowired
    private DynamoDbClient dynamoDbClient;

	
	@Value("${dynamodb.vendor.table}")
	private String vendorTable;

	@Autowired
	private VendorLogService logService;
	
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
	
	private List<Integer> lastFetchedZipCodes = new ArrayList<>();

	public List<Integer> getLastFetchedZipCodes() {
	    return lastFetchedZipCodes;
	}

	
	public List<Vendor> getAllVendors(){
		return vendorRepo.findAllVendors();
	}
	
	public VendorBatchResponseDTO saveAllVendors(List<Vendor> vendors) {
	    List<String> savedIds = new ArrayList<>();
	    List<String> skippedDuplicates = new ArrayList<>();

	    for (Vendor vendor : vendors) {
	        if (vendorRepo.findById(vendor.getVendorId()).isPresent()) {
	            skippedDuplicates.add(vendor.getVendorId());
	            continue; // Skipping duplicate
	        }

	        vendorRepo.saveVendor(vendor);
	        savedIds.add(vendor.getVendorId());
	    }

	    return new VendorBatchResponseDTO(savedIds, skippedDuplicates);
	}
	
	public List<Vendor> parseCSV(MultipartFile file) throws IOException {
	    List<Vendor> vendorList = new ArrayList<>();
	    int expectedColumns = 15; // Update if column count changes

	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
	        String line;
	        boolean isHeader = true;
	        while ((line = reader.readLine()) != null) {
	            if (isHeader) { isHeader = false; continue; }

	            String[] data = line.split(",", -1);
	            if (data.length < expectedColumns) {
	                System.out.println("Skipping row with insufficient columns: " + Arrays.toString(data));
	                continue;
	            }

	            try {
	                Vendor vendor = new Vendor();

	                vendor.setVendorId(data[0]);
	                vendor.setVendorName(data[1]);
	                vendor.setVendorLocation(Integer.parseInt(data[2]));
	                vendor.setIsVendorOpen(data[3]);
	                vendor.setIsVendorDeliverable(data[4]);
	                vendor.setVendorType(data[5]);
	                vendor.setContactEmail(data[6]);
	                vendor.setPhoneNumber(data[7]);
	                vendor.setRating(Double.parseDouble(data[8]));
	                vendor.setMaxDailyOrders(Integer.parseInt(data[9]));
	                vendor.setPreferredDeliveryWindow(data[10]);
	                vendor.setCreatedTimestamp(Long.parseLong(data[11]));
	                vendor.setCertified(Boolean.parseBoolean(data[12]));

	                if (!data[13].isBlank()) {
	                    List<Integer> zipCodes = Arrays.stream(data[13].split(";"))
	                        .map(String::trim)
	                        .map(Integer::parseInt)
	                        .collect(Collectors.toList());
	                    vendor.setServiceableZipCodes(zipCodes);
	                }

	                if (!data[14].isBlank()) {
	                    List<String> tags = Arrays.stream(data[14].split(";"))
	                        .map(String::trim)
	                        .collect(Collectors.toList());
	                    vendor.setTags(tags);
	                }

	                vendorList.add(vendor);

	            } catch (Exception ex) {
	                System.out.println("Skipping malformed row: " + Arrays.toString(data));
	                System.out.println("Reason: " + ex.getMessage());
	                // optionally log line number or vendorId if available
	                continue;
	            }
	        }
	    }

	    return vendorList;
	}
	
	public Optional<Vendor> resolveVendorByNameAndLocation(String vendorName, int location) {
		System.out.println("Inside vendor service");
		String vendorEncoded = vendorName.trim().toLowerCase().replaceAll("\\s+", " ");
	    try {
	        return vendorRepo.findByNameAndLocation(vendorEncoded, location);
	    } catch (Exception ex) {
	        log.error("Error while resolving vendor. Name={}, Location={}, Reason={}", 
	                  vendorName, location, ex.getMessage());
	        return Optional.empty(); // fallback
	    }
	}
	
	public Optional<String> deliveryvalidation(String vendorId) {
	    logService.logMessageToCloudWatch("[Vendor-Service] Validating delivery window for vendor: " + vendorId);
	    try {
	        GetItemRequest request = GetItemRequest.builder()
	            .tableName(vendorTable)
	            .key(Map.of("vendorId", AttributeValue.fromS(vendorId)))
	            .projectionExpression("preferredDeliveryWindow,serviceableZipCodes")
	            .build();

	        GetItemResponse response = dynamoDbClient.getItem(request);

	        if (response.hasItem()) {
	            Map<String, AttributeValue> item = response.item();

	            // 🌅 Preferred Delivery Window
	            String preferredWindow = Optional.ofNullable(item.get("preferredDeliveryWindow"))
	                                             .map(AttributeValue::s)
	                                             .orElse("UNKNOWN");

	            // 📍 Serviceable Zip Codes
	            if (item.containsKey("serviceableZipCodes")) {
	                List<Integer> zipCodes = item.get("serviceableZipCodes").l().stream()
	                    .map(AttributeValue::n)
	                    .map(Integer::parseInt)
	                    .collect(Collectors.toList());

	                this.lastFetchedZipCodes = zipCodes;
	                logService.logMessageToCloudWatch("[Vendor-Service] Serviceable zip codes for vendorId=" + vendorId + ": " + zipCodes);
	            }

	            return Optional.of(preferredWindow);
	        }
	    } catch (Exception e) {
	        System.err.println("Failed to fetch delivery config for vendorId=" + vendorId + ": " + e.getMessage());
	    }

	    this.lastFetchedZipCodes = new ArrayList<>(); // clear if error or not found
	    return Optional.empty();
	}

	
}

	
	


