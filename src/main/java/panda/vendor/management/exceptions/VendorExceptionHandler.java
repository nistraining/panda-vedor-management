package panda.vendor.management.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import panda.vendor.management.constants.ValidationMessages;

@ControllerAdvice
public class VendorExceptionHandler {
	
	
	 @ExceptionHandler(VendorException.class)
	    public ResponseEntity<Object> handleMandatoryFieldsException(VendorException ex) {
		   ErrorResponse error = new ErrorResponse(ex.getErrorCode(), ex.getErrorMessage());
	        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	    }


}
