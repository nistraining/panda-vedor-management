package panda.vendor.management.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.panda.vendor.management.entities.VendorResponseDTO;
import panda.vendor.management.dto.VendorOrderRequest;

import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;

@Service
public class KafkaDlqRetryService {

    @Value("${aws.dlq.url}")
    private String dlqURL;

    @Value("${topic.name}")
    private String topicName;

    @Autowired
    private SqsAsyncClient sqsClient;

    @Autowired
    private KafkaTemplate<String, VendorResponseDTO> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VendorLogService logService;

    @Scheduled(fixedDelay = 60000)
    public void scheduledReplayToKafka() {
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
            .queueUrl(dlqURL)
            .messageAttributeNames("All")
            .maxNumberOfMessages(10)
            .waitTimeSeconds(5)
            .build();

        List<Message> dlqMessages = sqsClient.receiveMessage(receiveRequest).join().messages();

        if (dlqMessages.isEmpty()) {
            logService.logMessageToCloudWatch("[DLQ Kafka Replay] ✅ No messages to retry. System is healthy.");
            return;
        }

        for (Message message : dlqMessages) {
            try {
                int retryCount = Optional.ofNullable(message.messageAttributes().get("retryCount"))
                    .map(attr -> Integer.parseInt(attr.stringValue()))
                    .orElse(0);

                VendorOrderRequest request = objectMapper.readValue(message.body(), VendorOrderRequest.class);

                // 🔎 Business rule re-validation
                boolean isDeliverable = request.getQuantity() <= 50 && request.getOrderLocation() == 101;
                if (!isDeliverable) {
                    logService.logMessageToCloudWatch("[DLQ Kafka Replay] ⛔ Validation failed on retry (undeliverable): orderId="
                        + request.getOrderId() + " → Discarding message.");
                    deleteFromDlq(message);
                    continue;
                }

                if (retryCount >= 5) {
                    logService.logMessageToCloudWatch("[DLQ Kafka Replay] ❌ Max retry reached for orderId="
                        + request.getOrderId() + " → Manual action needed.");
                    deleteFromDlq(message);
                    continue;
                }

                VendorResponseDTO response = new VendorResponseDTO();
                response.setOrderId(request.getOrderId());
                response.setOrderName(request.getOrderName());
                response.setQuantity(request.getQuantity());
                response.setOrderLocation(request.getOrderLocation());
                response.setDeliverable(true);
                response.setMessageType("RESPONSE");
                kafkaTemplate.send(topicName, response).get();
                logService.logMessageToCloudWatch("[DLQ Kafka Replay] ✅ Successfully republished to Kafka: orderId=" + request.getOrderId());
                deleteFromDlq(message);

            } catch (Exception ex) {
                int currentRetry = Optional.ofNullable(message.messageAttributes().get("retryCount"))
                    .map(attr -> Integer.parseInt(attr.stringValue()))
                    .orElse(0);

                // Requeue with incremented retryCount
                SendMessageRequest retryMsg = SendMessageRequest.builder()
                    .queueUrl(dlqURL)
                    .messageBody(message.body())
                    .messageAttributes(Map.of(
                        "retryCount", MessageAttributeValue.builder()
                            .dataType("Number")
                            .stringValue(String.valueOf(currentRetry + 1))
                            .build()
                    ))
                    .build();

                sqsClient.sendMessage(retryMsg);
                deleteFromDlq(message);
                logService.logMessageToCloudWatch("[DLQ Kafka Replay] 🔁 Retry failed, rescheduled with retryCount="
                    + (currentRetry + 1) + ": " + ex.getMessage());
            }
        }
    }

    private void deleteFromDlq(Message msg) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
            .queueUrl(dlqURL)
            .receiptHandle(msg.receiptHandle())
            .build());
    }
}