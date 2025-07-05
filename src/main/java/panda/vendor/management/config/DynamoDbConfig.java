package panda.vendor.management.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import panda.vendor.management.entities.Vendor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Configuration
public class DynamoDbConfig {


	@Bean
	public DynamoDbClient dynamoDbClient(AwsBasicCredentials awsBasicCredentials) {
		return DynamoDbClient.builder()
				.region(Region.EU_CENTRAL_1)
				.credentialsProvider(StaticCredentialsProvider
						.create(awsBasicCredentials))
				.build();
	}


	@Bean
	public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
		return DynamoDbEnhancedClient.builder()
				.dynamoDbClient(dynamoDbClient)
				.build();
	}

	@Bean
	public DynamoDbTable<Vendor> vendorTable(DynamoDbEnhancedClient enhancedClient) {
		return enhancedClient.table("vendor", TableSchema.fromBean(Vendor.class));
	}


}
