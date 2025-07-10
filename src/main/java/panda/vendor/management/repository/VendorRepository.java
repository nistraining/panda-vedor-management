package panda.vendor.management.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import panda.vendor.management.entities.Vendor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@Repository
public class VendorRepository {
	
	@Autowired
	private IdInitializer idInitializer;
	private final DynamoDbTable<Vendor> vendorTable;

	public VendorRepository(DynamoDbTable<Vendor> vendorTable) {
		super();
		this.vendorTable = vendorTable;
	}
	
	public Vendor saveVendor(Vendor vendor) {
		idInitializer.ensureId(vendor);
		vendorTable.putItem(vendor);
		return vendor;
	}
	
	public List<Vendor> findAllVendors(){
		return vendorTable.scan().items().stream().toList();
	}
	
	public Optional<Vendor> findById(String vendorId) {
        return Optional.ofNullable(
            vendorTable.getItem(r -> r.key(k -> k.partitionValue(vendorId)))
        );
    }
	
	public Optional<Vendor> findByNameAndLocation(String vendorName,int location){
		System.out.println("Inside the vendor repor");
		List<Vendor> matches = vendorTable.scan().items().stream()
				.filter(v->
				 v.getVendorName() != null && v.getVendorName().equalsIgnoreCase(vendorName) &&
				 v.getServiceableZipCodes() != null && v.getServiceableZipCodes().contains(location)
				 ).collect(Collectors.toList());
		return matches.stream().findFirst();
	}
	
	


	

}
