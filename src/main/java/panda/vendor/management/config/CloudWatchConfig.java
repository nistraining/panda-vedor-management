package panda.vendor.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

@Configuration
public class CloudWatchConfig {
	
	@Bean
	public CloudWatchLogsClient cloudWatchConfigClient(AwsBasicCredentials awsBasicCredentials) {
		return CloudWatchLogsClient.builder()
				.region(Region.EU_CENTRAL_1)
				.credentialsProvider(StaticCredentialsProvider
						.create(awsBasicCredentials))
				.build();
	}
	

}
