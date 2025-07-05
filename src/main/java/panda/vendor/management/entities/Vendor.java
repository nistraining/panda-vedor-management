package panda.vendor.management.entities;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Vendor {

	
	private String vendorId;
	private String vendorName;
	private int vendorLocation;
	private String isVendorOpen;
	private String isVendorDeliverable;
	
	@DynamoDbPartitionKey
	@DynamoDbAttribute("vendorId")
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	@DynamoDbAttribute("vendorName")
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	@DynamoDbAttribute("vendorLocation")
	public int getVendorLocation() {
		return vendorLocation;
	}
	public void setVendorLocation(int vendorLocation) {
		this.vendorLocation = vendorLocation;
	}
	@DynamoDbAttribute("isVendorOpen")
	public String getIsVendorOpen() {
		return isVendorOpen;
	}
	public void setIsVendorOpen(String isVendorOpen) {
		this.isVendorOpen = isVendorOpen;
	}
	@DynamoDbAttribute("isVendorDeliverable")
	public String getIsVendorDeliverable() {
		return isVendorDeliverable;
	}
	public void setIsVendorDeliverable(String isVendorDeliverable) {
		this.isVendorDeliverable = isVendorDeliverable;
	}
	
	
}
