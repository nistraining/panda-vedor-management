package panda.vendor.management.services;

import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import panda.vendor.management.dto.VendorOrderRequest;
import panda.vendor.management.dto.VendorResponseDTO;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class OrderValidationConsumer {

	@Autowired
	private SqsTemplate sqsTemplate;

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

	public void respondToOrderService(VendorOrderRequest request, boolean isDeliverable) {
		VendorResponseDTO vendorOrderResponseDTO = new VendorResponseDTO();
		vendorOrderResponseDTO.setOrderName(request.getOrderName());
		vendorOrderResponseDTO.setQuantity(request.getQuantity());
		vendorOrderResponseDTO.setOrderLocation(request.getOrderLocation());
		vendorOrderResponseDTO.setDeliverable(isDeliverable);
		vendorOrderResponseDTO.setMessageType("RESPONSE");
		try {
			sqsTemplate.send(
					"https://sqs.eu-central-1.amazonaws.com/489855987447/nisith-tech-queue",
					vendorOrderResponseDTO
					);


		} catch (Exception e) {
			System.err.println("Serialization or send error: " + e.getMessage());
		}

	}
}