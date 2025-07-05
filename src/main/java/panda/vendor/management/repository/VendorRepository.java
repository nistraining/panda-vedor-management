package panda.vendor.management.repository;

import java.util.List;
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

}
