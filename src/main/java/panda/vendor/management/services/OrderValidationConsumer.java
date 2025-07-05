package panda.vendor.management.services;

import java.util.Map;
import java.util.function.Consumer;

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
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class OrderValidationConsumer {
	
	@Autowired
	private KafkaTemplate<String, VendorResponseDTO> kafkaTemplate;
	
	@Autowired
	private SchemaRegistryUtils schemaRegistryUtils;
	@Value("${topic.name}")
	private String topicName;

	@SqsListener("https://sqs.eu-central-1.amazonaws.com/489855987447/panda-foods-queue")
	public void processOrderRequest(@Payload VendorOrderRequest request) {
		boolean isDeliverable=false;
		try {
			if(request.getQuantity() <= 50 && request.getOrderLocation() == 101) {
				isDeliverable=true;
			}
			System.out.println("Is deliveable :"+ isDeliverable);
			respondToOrderService(request, isDeliverable);
			System.out.println("After order response");
			System.out.println("Message processed successfully");
		}catch(Exception ex) {
			System.out.println("Failed to process the message :" +ex.getMessage());
		}
	}
	
	public void waitForSchemaRegistry() {
		
		int retries = 4;
		int delay = 5000;
	}

	public void respondToOrderService(VendorOrderRequest request, boolean isDeliverable) {
		VendorResponseDTO vendorOrderResponseDTO =new VendorResponseDTO();
		vendorOrderResponseDTO.setOrderName(request.getOrderName());
		vendorOrderResponseDTO.setQuantity(request.getQuantity());
		vendorOrderResponseDTO.setOrderLocation(request.getOrderLocation());
		vendorOrderResponseDTO.setDeliverable(isDeliverable);
		vendorOrderResponseDTO.setMessageType("RESPONSE");
		try {
			Thread.sleep(5000);
			schemaRegistryUtils.waitForSchemaRegistry();
			kafkaTemplate.send(topicName,vendorOrderResponseDTO);
			System.out.println("Sent Vendor response to kafka topic");
		}catch(Exception e) {
			System.out.println("Kafka publish error :"+ e.getMessage());
		}

	}
}