package com.example.applicationgateway.filter;

import com.example.applicationgateway.domain.User;
import com.example.applicationgateway.repository.UserRepository;
import com.example.applicationgateway.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GateWayFilter implements GlobalFilter {
    @Value("${final-project.paths.no-authentication}")
    private List<String> noAuthpaths = new ArrayList<>();
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserRepository userRepository;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if(isPathRequireNoAuthentication(request.getMethod().toString(), request.getPath().value())){
            return chain.filter(exchange);
        }
        String token = getAuthHeader(request);
        if (null!=token && token.startsWith("Bearer ") && token.length() > 7) {
            token = token.substring(7, token.length());
        }
        if(jwtService.isTokenValid(token)) {
            Claims claims = jwtService.extractAllClaims(token);
            String userEmail = (String) claims.get("userEmail");


            if(null == userEmail){
                return this.onError(exchange, "UNAUTHORIZED ACCESS, NO USEREMAIL or ADMIN CLAIMS IN TOKEN", HttpStatus.UNAUTHORIZED);
            }
            Optional<User> optionalUser = userRepository.findByEmail(userEmail);
            if(optionalUser.isPresent()){
                User user = optionalUser.get();
            }else{
                return this.onError(exchange, "UNAUTHORIZED ACCESS, USEREMAIL NOT FOUND", HttpStatus.UNAUTHORIZED);
            }



            request = request.mutate()
                    .header("userEmail", String.valueOf(userEmail))
                    .build();

            ServerWebExchange exchange1 = exchange.mutate().request(request).build();
            return chain.filter(exchange1);
        }else {
            return this.onError(exchange, "UNAUTHORIZED ACCESS", HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        response.setStatusCode(httpStatus);
        response.writeWith(Flux.just(buffer));
        return response.setComplete();
    }

    private String getAuthHeader(ServerHttpRequest request) {
        try {
            return request.getHeaders().getOrEmpty("Authorization").get(0);
        }catch (Exception e){
            return null;
        }
    }

    private boolean isPathRequireNoAuthentication(String method, String path){
        return isPathsMatched(method, path, noAuthpaths);
    }



    private boolean isPathsMatched(String method, String path, List<String> regexPaths){
        String pathAndMethod = (method + " " + path).toLowerCase();
        boolean result = false;
        for(String regexPath: regexPaths){
            regexPath = regexPath.toLowerCase();
//            System.out.println(pathAndMethod + " - " + regexPath);
            if(pathAndMethod.matches(regexPath)){
                result = true;
//                System.out.println(pathAndMethod + " - " + regexPath + " - " + result);
                break;
            }

        }
        return result;


    }

}
