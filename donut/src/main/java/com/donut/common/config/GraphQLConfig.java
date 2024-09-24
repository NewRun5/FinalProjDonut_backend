package com.donut.common.config;

import graphql.schema.GraphQLScalarType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
@RequiredArgsConstructor
public class GraphQLConfig {
    private final GraphQLScalarType localDateScalar;
    private final GraphQLScalarType localDateTimeScalarType;

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return builder -> builder
                .scalar(localDateScalar)
                .scalar(localDateTimeScalarType);
    }
}