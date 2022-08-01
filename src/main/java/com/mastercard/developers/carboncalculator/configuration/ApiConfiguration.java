/*
 *  Copyright (c) 2021 Mastercard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mastercard.developers.carboncalculator.configuration;

import com.mastercard.developer.interceptors.OkHttpOAuth1Interceptor;
import com.mastercard.developer.utils.AuthenticationUtils;
import com.mastercard.developers.carboncalculator.util.FileResourcesUtils;
import com.mastercard.developers.carboncalculator.util.StreamUtil;
import org.openapitools.client.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.PrivateKey;

/**
 * Api Client Setup
 */

@Configuration
public class ApiConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiConfiguration.class);
    @Autowired
    private ResourceLoader resourceLoader;
    @Value("${mastercard.api.authentication.consumer-key}")
    private String consumerKey;

    @Value("${mastercard.api.authentication.keystore-alias}")
    private String keyAlias;

    @Value("${mastercard.api.authentication.keystore-password}")
    private String keyPassword;

    @Value("${mastercard.api.authentication.key-file}")
    private String p12File;

    @Value("${mastercard.api.environment.base-path}")
    private String basePath;

    @Value("${mastercard.api.encryption.key-file}")
    private String encryptionKeyFile;

    @Value("${mastercard.api.encryption.fingerprint}")
    private String encryptionFingerprint;

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getEncryptionKeyFile() throws URISyntaxException, IOException {

        //"C:\\Users\\surya\\IdeaProjects\\carbon-calculator-reference-app\\cert\\carbon_calculatorClientEnc1659120391.pem";
        return createAbsoluteFile(encryptionKeyFile).getAbsolutePath();
    }

    @Bean
    public ApiClient setupApiClient() {
        var apiClient = new ApiClient();

        apiClient.setBasePath(basePath);
        apiClient.setHttpClient(
                apiClient.getHttpClient()
                        .newBuilder()
                        .addInterceptor(new OkHttpOAuth1Interceptor(consumerKey, getSigningKey()))
                        .build()
        );
        apiClient.setDebugging(false);

        return apiClient;
    }

    public PrivateKey getSigningKey() {
        try {
            File file = createAbsoluteFile(p12File);
            //"C:\\Users\\surya\\IdeaProjects\\carbon-calculator-reference-app\\cert\\keyalias-sandbox.p12"
            return AuthenticationUtils.loadSigningKey(
                    file.getAbsolutePath(),
                    keyAlias,
                    keyPassword);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public File createAbsoluteFile(String fileName) throws URISyntaxException, IOException {
        FileResourcesUtils app = new FileResourcesUtils();
        InputStream is = app.getFileFromResourceAsStream(fileName);
        LOGGER.info("{} found {}", fileName, is.available());
        File file = StreamUtil.stream2file(is);
        LOGGER.info("{} --> temp file {}", fileName, file);
        return file;
    }

}
