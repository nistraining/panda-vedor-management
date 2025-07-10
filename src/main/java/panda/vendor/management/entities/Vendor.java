package panda.vendor.management.entities;

import java.util.List;

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

	private String vendorType;
	private String contactEmail;
	private String phoneNumber;
	private double rating;
	private int maxDailyOrders;
	private String preferredDeliveryWindow;
	private long createdTimestamp;
	private boolean isCertified;
	private List<Integer> serviceableZipCodes;
	private List<String> tags;


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
	@DynamoDbAttribute("vendorType")
	public String getVendorType() {
		return vendorType;
	}
	public void setVendorType(String vendorType) {
		this.vendorType = vendorType;
	}
	@DynamoDbAttribute("contactEmail")
	public String getContactEmail() {
		return contactEmail;
	}
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}
	@DynamoDbAttribute("phoneNumber")
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	@DynamoDbAttribute("rating")
	public double getRating() {
		return rating;
	}
	public void setRating(double rating) {
		this.rating = rating;
	}
	@DynamoDbAttribute("maxDailyOrders")
	public int getMaxDailyOrders() {
		return maxDailyOrders;
	}
	public void setMaxDailyOrders(int maxDailyOrders) {
		this.maxDailyOrders = maxDailyOrders;
	}
	@DynamoDbAttribute("preferredDeliveryWindow")
	public String getPreferredDeliveryWindow() {
		return preferredDeliveryWindow;
	}
	public void setPreferredDeliveryWindow(String preferredDeliveryWindow) {
		this.preferredDeliveryWindow = preferredDeliveryWindow;
	}
	@DynamoDbAttribute("createdTimestamp")
	public long getCreatedTimestamp() {
		return createdTimestamp;
	}
	public void setCreatedTimestamp(long createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}
	@DynamoDbAttribute("isCertified")
	public boolean isCertified() {
		return isCertified;
	}
	public void setCertified(boolean isCertified) {
		this.isCertified = isCertified;
	}
	@DynamoDbAttribute("serviceableZipCodes")
	public List<Integer> getServiceableZipCodes() {
		return serviceableZipCodes;
	}
	public void setServiceableZipCodes(List<Integer> serviceableZipCodes) {
		this.serviceableZipCodes = serviceableZipCodes;
	}
	@DynamoDbAttribute("tags")
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	


}
