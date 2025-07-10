package panda.vendor.management.services;

import java.time.Instant;
import java.util.Collections;

import javax.management.RuntimeErrorException;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceAlreadyExistsException;

@Service
public class VendorLogService {

	private final CloudWatchLogsClient cloudWatchLogsClient;
	private final String logGroupName = "panda-food-orders";
	private final String logStreamName = "panda-food-streams";
	private String nextSequenceToken;
	
	public VendorLogService(CloudWatchLogsClient cloudWatchLogsClient) {
		this.cloudWatchLogsClient = cloudWatchLogsClient;
        try {
        createLogResources();
    }catch(Exception e){
    	System.out.println("Failed to initialize cloudwatch resource : " +e.getMessage());
    }
    }

    private void createLogResources() {
        try {
            cloudWatchLogsClient.createLogGroup(CreateLogGroupRequest.builder()
                    .logGroupName(logGroupName)
                    .build());
        } catch (ResourceAlreadyExistsException ignored) {}

        try {
            cloudWatchLogsClient.createLogStream(CreateLogStreamRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName)
                    .build());
        } catch (ResourceAlreadyExistsException ignored) {}

        DescribeLogStreamsResponse response = cloudWatchLogsClient.describeLogStreams(
            DescribeLogStreamsRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamNamePrefix(logStreamName)
                    .build());

        nextSequenceToken = response.logStreams().stream()
                .filter(s -> s.logStreamName().equals(logStreamName))
                .map(LogStream::uploadSequenceToken)
                .findFirst()
                .orElse(null);

        if (nextSequenceToken == null) {
            System.out.println("⚠️ No sequence token found — log stream might not exist yet or hasn't propagated");
        }
    }

    public void logMessageToCloudWatch(String message) {
        InputLogEvent logEvent = InputLogEvent.builder()
                .message(message)
                .timestamp(Instant.now().toEpochMilli())
                .build();

        PutLogEventsRequest.Builder request = PutLogEventsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamName(logStreamName)
                .logEvents(Collections.singletonList(logEvent));

        if (nextSequenceToken != null) {
            request.sequenceToken(nextSequenceToken);
        }

        try {
            PutLogEventsResponse response = cloudWatchLogsClient.putLogEvents(request.build());
            nextSequenceToken = response.nextSequenceToken();
        } catch (Exception e) {
            System.err.println("❌ Failed to push logs to CloudWatch: " + e.getMessage());
        }
    }


}
