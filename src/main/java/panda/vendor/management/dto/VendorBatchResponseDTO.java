package panda.vendor.management.dto;

import java.util.List;

public class VendorBatchResponseDTO {
	
	private List<String> savedVendorIds;
    private List<String> skippedDuplicates;
    
    
    
    
	public VendorBatchResponseDTO() {
	}


	public VendorBatchResponseDTO(List<String> savedVendorIds, List<String> skippedDuplicates) {
		this.savedVendorIds = savedVendorIds;
		this.skippedDuplicates = skippedDuplicates;
	}


	public List<String> getSavedVendorIds() {
		return savedVendorIds;
	}


	public void setSavedVendorIds(List<String> savedVendorIds) {
		this.savedVendorIds = savedVendorIds;
	}


	public List<String> getSkippedDuplicates() {
		return skippedDuplicates;
	}


	public void setSkippedDuplicates(List<String> skippedDuplicates) {
		this.skippedDuplicates = skippedDuplicates;
	}
    
	
    


}
