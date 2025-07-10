package panda.vendor.management.services;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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
	private VendorRepository vendorRepository;
	
	@Value("${topic.name}")
	private String topicName;
	
	@Value("${aws.dlq.url}")
	private String dlqName;


	@SqsListener("https://sqs.eu-central-1.amazonaws.com/489855987447/panda-foods-queue")
	public void processOrderRequest(@Payload VendorOrderRequest request) {
		boolean isDeliverable=false;
		int orderId = request.getOrderId();
		String correlationId = correlationServiceUtils.generateForOrder(orderId);
		logService.logMessageToCloudWatch("[Vendor-Service] Received orderId=" + orderId + ", corrId=" + correlationId);
		try {
			if(request.getQuantity() <= 50 && request.getOrderLocation() == 101) {
				isDeliverable=true;
			}		
			//Sending to Kafka
			respondToOrderService(request, isDeliverable);
			
			//logging the success response
			logService.logMessageToCloudWatch("[Vendor-Service] Message processed: orderId=" + orderId + ", corrId=" + correlationId);
		} catch (Exception ex) {
			//logging to the DLQ
			logService.logMessageToCloudWatch("[Vendor-Service] Failed to process orderId=" + orderId + ", corrId=" + correlationId + ", error=" + ex.getMessage());
		    throw ex;
		} finally {
			MDC.clear();
		}
	}


	public void waitForSchemaRegistry() {

		int retries = 4;
		int delay = 5000;
	}

	public void respondToOrderService(VendorOrderRequest request, boolean isDeliverable) {
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


	    int orderId = request.getOrderId();
	    String correlationId = correlationServiceUtils.getCorrelationId(orderId).orElse("UNKNOWN");

	    try {
	        Thread.sleep(5000); // Simulated delay
	        schemaRegistryUtils.waitForSchemaRegistry();
	        logService.logMessageToCloudWatch(topicName);
	        kafkaTemplate.send(topicName, response).get(); // block until confirmed
	        logService.logMessageToCloudWatch("[Vendor-Service] Kafka response sent: orderId=" + orderId + ", corrId=" + correlationId);
	    } catch (Exception e) {
	        logService.logMessageToCloudWatch("[Vendor-Service] Kafka publish failed: orderId=" + orderId + ", corrId=" + correlationId + ", error=" + e.getMessage() +", cause=" +e.getCause());

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