package panda.vendor.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Configuration
public class SecretConfig {
	
	@Bean
    public AwsBasicCredentials awsBasicCredentials() {
        SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .region(Region.of("eu-central-1"))
                .build();

        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId("pandafoodsCredentials")
                .build();

        GetSecretValueResponse response = secretsClient.getSecretValue(request);
        JsonObject creds = new Gson().fromJson(response.secretString(), JsonObject.class);

        return AwsBasicCredentials.create(
                creds.get("cloud.aws.credentials.access-key").getAsString(),
                creds.get("cloud.aws.credentials.secret-key").getAsString()
        );
    }
	

}
