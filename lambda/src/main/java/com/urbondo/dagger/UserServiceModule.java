package com.urbondo.dagger;

import com.urbondo.api.user.repository.UserRepositoryImpl;
import com.urbondo.api.user.service.CognitoService;
import com.urbondo.api.user.service.CognitoServiceImpl;
import com.urbondo.api.user.service.UserService;
import com.urbondo.api.user.service.UserServiceImpl;
import com.urbondo.lib.DynamoDBConfig;
import dagger.Module;
import dagger.Provides;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static software.amazon.awssdk.regions.Region.US_EAST_1;

@Module
public class UserServiceModule {

    public static final String AWS_COGNITO_CLIENT_ID = "aws.cognito.clientId";
    public static final String AWS_COGNITO_CLIENT_SECRET = "aws.cognito.clientSecret";

    @Singleton
    @Provides
    public UserService userService() {
        return new UserServiceImpl(new UserRepositoryImpl(new DynamoDBConfig().dynamoDBMapper()));
    }

    @Singleton
    @Provides
    public CognitoService cognitoService() {
        return new CognitoServiceImpl(
                CognitoIdentityProviderClient.builder()
                        .region(US_EAST_1)
                        .credentialsProvider(ProfileCredentialsProvider.create())
                        .build());
    }

    @Singleton
    @Provides
    @Named("clientId")
    String provideClientId() {
        Optional<PropertySource<?>> propertySource = getApplicationPropertySource();

        if (propertySource.isPresent() && propertySource.get().containsProperty(AWS_COGNITO_CLIENT_ID)) {
            return Objects.requireNonNull(propertySource.get().getProperty(AWS_COGNITO_CLIENT_ID)).toString();
        }

        return "";
    }


    @Singleton
    @Provides
    @Named("clientSecret")
    String provideClientSecret() {
        Optional<PropertySource<?>> propertySource = getApplicationPropertySource();

        if (propertySource.isPresent() && propertySource.get().containsProperty(AWS_COGNITO_CLIENT_SECRET)) {
            return Objects.requireNonNull(propertySource.get().getProperty(AWS_COGNITO_CLIENT_SECRET)).toString();
        }

        return "";
    }

    private static Optional<PropertySource<?>> getApplicationPropertySource() {

        try {
            return new YamlPropertySourceLoader().load("application-cognito.yaml",
                                                       new ClassPathResource("application-cognito.yaml"))
                    .stream()
                    .findFirst();
        } catch (IOException exception) {
            return Optional.empty();
        }
    }
}
