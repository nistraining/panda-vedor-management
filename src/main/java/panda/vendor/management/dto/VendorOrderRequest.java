package panda.vendor.management.dto;

import java.time.Instant;

public class VendorOrderRequest {
	
	private String messageType;
	private String orderName;
	private int orderId;
    private int quantity;
    private int orderLocation;
    private Instant createdTimeStamp;
    private String vendorId;

    
    
    
	public VendorOrderRequest() {
		super();
	}




	public VendorOrderRequest(String messageType, String orderName, int orderId, int quantity, int orderLocation,
			Instant createdTimeStamp, String vendorId) {
		super();
		this.messageType = messageType;
		this.orderName = orderName;
		this.orderId = orderId;
		this.quantity = quantity;
		this.orderLocation = orderLocation;
		this.createdTimeStamp = createdTimeStamp;
		this.vendorId=vendorId;
	}




	public String getMessageType() {
		return messageType;
	}




	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}




	public String getOrderName() {
		return orderName;
	}




	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}




	public int getOrderId() {
		return orderId;
	}




	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}




	public int getQuantity() {
		return quantity;
	}




	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}




	public int getOrderLocation() {
		return orderLocation;
	}




	public void setOrderLocation(int orderLocation) {
		this.orderLocation = orderLocation;
	}




	public Instant getCreatedTimeStamp() {
		return createdTimeStamp;
	}




	public void setCreatedTimeStamp(Instant createdTimeStamp) {
		this.createdTimeStamp = createdTimeStamp;
	}




	public String getVendorId() {
		return vendorId;
	}




	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	
	
	
	
	
    
    


}
