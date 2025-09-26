package com.openisle.config;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClientBuilder;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenSearchProperties.class)
@ConditionalOnProperty(prefix = "opensearch", name = "enabled", havingValue = "true")
public class OpenSearchConfig {

  private RestClient restClient;

  @Bean
  public RestClient openSearchRestClient(OpenSearchProperties properties) {
    List<String> hosts = properties.getHosts();
    if (hosts == null || hosts.isEmpty()) {
      throw new IllegalStateException(
        "opensearch.hosts must be configured when OpenSearch is enabled"
      );
    }

    HttpHost[] httpHosts = hosts.stream().map(HttpHost::create).toArray(HttpHost[]::new);

    RestClient.Builder builder = RestClient.builder(httpHosts);

    builder.setRequestConfigCallback(requestConfigBuilder -> {
      RequestConfig.Builder config = RequestConfig.custom();
      config.setConnectTimeout(properties.getConnectTimeout());
      config.setResponseTimeout(properties.getSocketTimeout());
      return config;
    });

    builder.setHttpClientConfigCallback(clientBuilder -> {
      HttpClientBuilder httpClientBuilder = clientBuilder;
      if (properties.getUsername() != null && properties.getPassword() != null) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
          new AuthScope(null, -1),
          new UsernamePasswordCredentials(
            properties.getUsername(),
            properties.getPassword().toCharArray()
          )
        );
        httpClientBuilder = httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      }

      if (properties.isInsecure()) {
        try {
          SSLContext sslContext = SSLContext.getInstance("TLS");
          sslContext.init(
            null,
            new TrustManager[] {
              new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                  return new java.security.cert.X509Certificate[0];
                }

                public void checkClientTrusted(
                  java.security.cert.X509Certificate[] chain,
                  String authType
                ) {}

                public void checkServerTrusted(
                  java.security.cert.X509Certificate[] chain,
                  String authType
                ) {}
              },
            },
            new java.security.SecureRandom()
          );
          httpClientBuilder = httpClientBuilder.setSSLContext(sslContext);
        } catch (Exception e) {
          throw new IllegalStateException("Failed to configure insecure SSL context", e);
        }
      }
      return httpClientBuilder;
    });

    restClient = builder.build();
    return restClient;
  }

  @Bean
  public OpenSearchTransport openSearchTransport(RestClient restClient) {
    JacksonJsonpMapper mapper = new JacksonJsonpMapper();
    return new RestClientTransport(restClient, mapper);
  }

  @Bean
  public OpenSearchClient openSearchClient(OpenSearchTransport transport) {
    return new OpenSearchClient(transport);
  }

  @PreDestroy
  public void closeClient() throws IOException {
    if (restClient != null) {
      restClient.close();
    }
  }
}
