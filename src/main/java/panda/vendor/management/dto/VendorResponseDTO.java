package panda.vendor.management.dto;

public class VendorResponseDTO {
	
	private String orderName;
	private int quantity;
	private int orderLocation;
	private boolean isDeliverable;
    private String messageType;	
	public String getOrderName() {
		return orderName;
	}
	public void setOrderName(String orderName) {
		this.orderName = orderName;
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
	public boolean isDeliverable() {
		return isDeliverable;
	}
	public void setDeliverable(boolean isDeliverable) {
		this.isDeliverable = isDeliverable;
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	
	
	

}
