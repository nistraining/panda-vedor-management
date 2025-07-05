package panda.vendor.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class VendorSQSConfig {

	
	@Bean
	public SqsAsyncClient sqsAsyncClient(AwsBasicCredentials awsBasicCredentials) {
		return SqsAsyncClient.builder()
				.region(Region.EU_CENTRAL_1)
				.credentialsProvider(StaticCredentialsProvider
						.create(awsBasicCredentials))
				.build();
	}
	
	@Primary
	@Bean
	public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
		return SqsTemplate.builder().sqsAsyncClient(sqsAsyncClient).build();
	}

}
