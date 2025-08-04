package panda.vendor.management.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Configuration
public class SecretConfig {

    private static final String ACCESS_KEY_PATH = "/run/secrets/aws_access";
    private static final String SECRET_KEY_PATH = "/run/secrets/aws_secret";
    private static final String REGION = "eu-central-1";
    private static final String SECRET_ID = "pandafoodsCredentials";

    /**
     * Reads AWS credentials from Docker secrets.
     */
    @Bean
    @Qualifier("bootstrapAwsCredentials")
    public AwsBasicCredentials bootstrapAwsCredentials() throws IOException {
        String accessKey = Files.readString(Paths.get(ACCESS_KEY_PATH)).trim();
        String secretKey = Files.readString(Paths.get(SECRET_KEY_PATH)).trim();
        return AwsBasicCredentials.create(accessKey, secretKey);
    }

    /**
     * Creates a SecretsManagerClient using bootstrapped credentials.
     */
    @Bean
    public SecretsManagerClient secretsManagerClient(@Qualifier("bootstrapAwsCredentials") AwsBasicCredentials bootstrapCredentials) {
        return SecretsManagerClient.builder()
                .region(Region.of(REGION))
                .credentialsProvider(StaticCredentialsProvider.create(bootstrapCredentials))
                .build();
    }

    /**
     * Fetches final credentials from AWS Secrets Manager.
     */
    @Bean
    @Qualifier("awsBasicCredentials")
    public AwsBasicCredentials awsBasicCredentials(SecretsManagerClient secretsClient) {
        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(SECRET_ID)
                .build();

        GetSecretValueResponse response = secretsClient.getSecretValue(request);
        JsonObject creds = new Gson().fromJson(response.secretString(), JsonObject.class);

        return AwsBasicCredentials.create(
                creds.get("cloud.aws.credentials.access-key").getAsString(),
                creds.get("cloud.aws.credentials.secret-key").getAsString()
        );
    }
}