package com.starling.roundup.config;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCounted;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.List;

@Configuration
public class WebClientConfig {

    private final String CONTENT_LENGTH = "content-length";
    private final StarlingApiConfig starlingApiConfig;

    public WebClientConfig(StarlingApiConfig starlingApiConfig) {
        this.starlingApiConfig = starlingApiConfig;
    }

    /**
     * Created  custom webclient as The Spring WebClient automatically adds a Content-Length: 0 header
     * to all requests, including GET requests, even if there is no request body.
     * This is typically fine for most services, but it causes issues with the Starling API,
     * which expects no Content-Length header for GET requests.
     * This method will help remove the content-length header.
     */
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .doOnConnected(connection -> connection.addHandlerFirst(new MessageToMessageEncoder<HttpRequest>() {

                    @Override
                    protected void encode(ChannelHandlerContext ctx, HttpRequest msg, List<Object> out) {
                        HttpHeaders requestHeaders = msg.headers();
                        if (msg.method() == HttpMethod.GET) {
                            if (requestHeaders.contains(CONTENT_LENGTH)) {
                                requestHeaders.remove(CONTENT_LENGTH);
                            }
                        }

                        if (msg instanceof ReferenceCounted) {
                            ((ReferenceCounted) msg).retain();
                        }
                        out.add(msg);
                    }
                }));
        return WebClient.builder()
                .baseUrl(starlingApiConfig.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

    }
}