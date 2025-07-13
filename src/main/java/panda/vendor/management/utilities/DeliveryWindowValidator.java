package panda.vendor.management.utilities;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import panda.vendor.management.services.VendorLogService;

public class DeliveryWindowValidator {
	
	public static boolean isOrderWithinWindow(Instant instant, String preferredWindow, ZoneId vendorZone) {
        try {
            // Parse window string (e.g. "12:00–16:00")
            String[] timeRange = preferredWindow.split("–"); // Make sure it's an em dash, not hyphen
            if (timeRange.length != 2) {
                System.err.println("Invalid preferredWindow format: " + preferredWindow);
                return false;
            }

            LocalTime start = LocalTime.parse(timeRange[0].trim());
            LocalTime end = LocalTime.parse(timeRange[1].trim());

            // Convert order timestamp to vendor's local time
            LocalDateTime createdDateTime = LocalDateTime.ofInstant(instant, vendorZone);
            LocalTime orderTime = createdDateTime.toLocalTime();
            
            
            System.out.println("Window Start=" + start + ", End=" + end + ", OrderTime=" + orderTime);
            
            // Validate time window
            return !orderTime.isBefore(start) && !orderTime.isAfter(end);
        } catch (Exception e) {
            System.err.println("Failed to validate delivery window: " + e.getMessage());
            return false; // Fail-safe default
        }
    }


}
