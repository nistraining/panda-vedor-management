package panda.vendor.management.services;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.panda.vendor.management.entities.VendorResponseDTO;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import panda.vendor.management.config.SchemaRegistryUtils;
import panda.vendor.management.dto.VendorOrderRequest;
import panda.vendor.management.entities.Vendor;
import panda.vendor.management.repository.VendorRepository;
import panda.vendor.management.utilities.DeliveryWindowValidator;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class OrderValidationConsumer {

	@Autowired
	private KafkaTemplate<String, VendorResponseDTO> kafkaTemplate;

	@Autowired
	private VendorLogService logService;

	@Autowired
	private SchemaRegistryUtils schemaRegistryUtils;

	@Autowired
	private CorrelationServiceUtils correlationServiceUtils;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private SqsAsyncClient sqsasyncClient;
	
	@Autowired
	private KafkaDlqRetryService kafkaDlqRetryService;
	
	@Autowired
	private VendorServices vendorServices;

	
	@Value("${topic.name}")
	private String topicName;
	
	@Value("${aws.dlq.url}")
	private String dlqName;


	@SqsListener("https://sqs.eu-central-1.amazonaws.com/489855987447/panda-foods-queue")
	public void processOrderRequest(@Payload VendorOrderRequest request) {
	    int orderId = request.getOrderId();
	    String correlationId = correlationServiceUtils.generateForOrder(orderId);
	    MDC.put(correlationId, correlationId);

	    logService.logMessageToCloudWatch("[Vendor-Service] Received orderId=" + orderId + ", corrId=" + correlationId);

	    try {
	        boolean isDeliverable = false;
	        List<String> rejectionReasons = new ArrayList<>();
	        ZoneId vendorZone = ZoneId.of("Europe/Berlin");

	        Optional<String> deliveryValidation = vendorServices.deliveryvalidation(request.getVendorId());
	        String preferredWindow = deliveryValidation.orElse("UNKNOWN");

	        logService.logMessageToCloudWatch("Preferred window is: " + preferredWindow);
	        List<Integer> serviceableZipCodes = vendorServices.getLastFetchedZipCodes();
	        logService.logMessageToCloudWatch("Serviceable zipCodes: " + serviceableZipCodes);

	        // Timestamp conversion — adjust based on actual field type
	        Instant orderInstant = request.getCreatedTimeStamp(); // if Instant
	        // Instant orderInstant = Instant.ofEpochMilli(request.getCreatedTimeStamp()); // if Long

	        if (preferredWindow.equals("UNKNOWN")) {
	            rejectionReasons.add("Missing preferred delivery window");
	        }

	        boolean withinWindow = DeliveryWindowValidator.isOrderWithinWindow(orderInstant, preferredWindow, vendorZone);
	        if (!withinWindow) {
	            rejectionReasons.add("Order time outside preferred delivery window");
	        }

	        boolean isQuantityValid = request.getQuantity() > 0 && request.getQuantity() <= 50;
	        if (!isQuantityValid) {
	            rejectionReasons.add("Invalid quantity: " + request.getQuantity());
	        }

	        boolean isLocationServiceable = serviceableZipCodes.contains(request.getOrderLocation());
	        if (!isLocationServiceable) {
	            rejectionReasons.add("Location not serviceable: " + request.getOrderLocation());
	        }

	        isDeliverable = rejectionReasons.isEmpty();
	        logService.logMessageToCloudWatch("Is deliverable: " + isDeliverable);

	        respondToOrderService(request, isDeliverable, rejectionReasons);

	        logService.logMessageToCloudWatch("[Vendor-Service] Message processed: orderId=" + orderId + ", corrId=" + correlationId);
	    } catch (Exception ex) {
	        logService.logMessageToCloudWatch("[Vendor-Service] Failed to process orderId=" + orderId +
	            ", corrId=" + correlationId + ", error=" + ex.getMessage());
	        throw ex;
	    } finally {
	        MDC.clear();
	    }
	}
	public void waitForSchemaRegistry() {

		int retries = 4;
		int delay = 5000;
	}

	public void respondToOrderService(VendorOrderRequest request, boolean isDeliverable, List<String> rejectionReasons) {
	    VendorResponseDTO response = new VendorResponseDTO();
	    response.setOrderId(request.getOrderId());
	    response.setOrderName(request.getOrderName());
	    response.setQuantity(request.getQuantity());
	    response.setOrderLocation(request.getOrderLocation());
	    response.setDeliverable(isDeliverable);
	    response.setMessageType("RESPONSE");

	    if (request.getCreatedTimeStamp() != null) {
	        response.setCreatedTimestamp(request.getCreatedTimeStamp());
	    } else {
	        response.setCreatedTimestamp(null);
	    }
	    
	    // ✏️ Only set rejectionReasons if not deliverable
	    if (!isDeliverable && rejectionReasons != null && !rejectionReasons.isEmpty()) {
	        List<CharSequence> compatibleReasons = rejectionReasons.stream()
	            .map(reason -> (CharSequence) reason)
	            .collect(Collectors.toList());

	        response.setRejectionReasons(compatibleReasons);
	        logService.logMessageToCloudWatch("[Vendor-Service] Rejection reasons: " + String.join("; ", rejectionReasons));

	    int orderId = request.getOrderId();
	    String correlationId = correlationServiceUtils.getCorrelationId(orderId).orElse("UNKNOWN");

	    try {
	        Thread.sleep(5000); // Simulated delay
	        schemaRegistryUtils.waitForSchemaRegistry();
	        logService.logMessageToCloudWatch(topicName);
	        kafkaTemplate.send(topicName, response).get(); // block until confirmed

	        logService.logMessageToCloudWatch("[Vendor-Service] Kafka response sent: orderId=" + orderId + ", corrId=" + correlationId);
	    } catch (Exception e) {
	        logService.logMessageToCloudWatch("[Vendor-Service] Kafka publish failed: orderId=" + orderId +
	            ", corrId=" + correlationId + ", error=" + e.getMessage() + ", cause=" + e.getCause());

	        try {
	            String fallbackJson = objectMapper.writeValueAsString(request);
	            SendMessageRequest dlqMessage = SendMessageRequest.builder()
	                .queueUrl(dlqName)
	                .messageBody(fallbackJson)
	                .messageAttributes(Map.of(
	                    "retryCount", MessageAttributeValue.builder()
	                        .dataType("Number")
	                        .stringValue("0")
	                        .build()
	                ))
	                .build();

	            sqsasyncClient.sendMessage(dlqMessage);
	            logService.logMessageToCloudWatch("[Vendor-Service DLQ] 📭 Routed to DLQ: orderId=" + orderId);
	        } catch (Exception ex) {
	            logService.logMessageToCloudWatch("[Vendor-Service DLQ] ❗ Failed to push to DLQ: orderId=" + orderId + ", error=" + ex.getMessage());
	        }
	    }
	}
}
}