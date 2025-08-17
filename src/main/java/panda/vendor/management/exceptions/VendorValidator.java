package panda.vendor.management.exceptions;

import panda.vendor.management.exceptions.VendorException;
import panda.vendor.management.exceptions.Validator;
import panda.vendor.management.constants.ValidationMessages;
import panda.vendor.management.entities.Vendor;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class VendorValidator implements Validator<Vendor> {

    @Override
    public void validate(Vendor vendor) {
        if (vendor == null) {
            throw new VendorException(ValidationMessages.ERROR_CODE_0, ValidationMessages.ERR_MSG_0);
        }

        if (isBlank(vendor.getVendorName())) {
            throw new VendorException(ValidationMessages.ERROR_CODE_1, ValidationMessages.ERR_MSG_1); // e.g., "Vendor name is mandatory"
        }

        if (vendor.getVendorLocation() <= 0) {
            throw new VendorException(ValidationMessages.ERROR_CODE_2, ValidationMessages.ERR_MSG_2);
        }

        if (isBlank(vendor.getContactEmail())) {
            throw new VendorException(ValidationMessages.ERROR_CODE_3, ValidationMessages.ERR_MSG_3);
        }

        if (isBlank(vendor.getPhoneNumber())) {
            throw new VendorException(ValidationMessages.ERROR_CODE_4, ValidationMessages.ERR_MSG_4);
        }

        if (vendor.getRating() < 0 || vendor.getRating() > 5) {
            throw new VendorException(ValidationMessages.ERROR_CODE_5, ValidationMessages.ERR_MSG_5);
        }

        if (vendor.getServiceableZipCodes() == null || vendor.getServiceableZipCodes().isEmpty()) {
            throw new VendorException(ValidationMessages.ERROR_CODE_6, ValidationMessages.ERR_MSG_6);
        }

        if (vendor.getTags() == null || vendor.getTags().isEmpty()) {
            throw new VendorException(ValidationMessages.ERROR_CODE_7, "Vendor tags must not be empty");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}