package com.donut.common.scalarType;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Base64FileScalarConfig {

    @Bean
    public GraphQLScalarType base64FileScalar() {
        return GraphQLScalarType.newScalar()
                .name("Base64File")
                .description("Base64 encoded string representing a file")
                .coercing(base64FileCoercing())
                .build();
    }

    private Coercing<File, String> base64FileCoercing() {
        return new Coercing<File, String>() {


            @Override
            public File parseValue(@NotNull Object input,
                                   @NotNull GraphQLContext graphQLContext,
                                   @NotNull Locale locale) throws CoercingParseValueException {
                if (input instanceof String) {
                    try {
                        // Base64 문자열을 디코딩하여 파일로 저장
                        byte[] decodedBytes = Base64.getDecoder().decode((String) input);
                        File tempFile = createTempFile(decodedBytes);
                        return tempFile;
                    } catch (IllegalArgumentException | IOException e) {
                        throw new CoercingParseValueException("Invalid Base64 string or file write error");
                    }
                }
                throw new CoercingParseValueException("Expected a Base64 encoded string");
            }

            @Override
            public File parseLiteral(@NotNull Value<?> input,
                                     @NotNull CoercedVariables variables,
                                     @NotNull GraphQLContext graphQLContext,
                                     @NotNull Locale locale) throws CoercingParseLiteralException {
                if (input instanceof StringValue) {
                    try {
                        String base64String = ((StringValue) input).getValue();
                        byte[] decodedBytes = Base64.getDecoder().decode(base64String);
                        return createTempFile(decodedBytes);
                    } catch (IllegalArgumentException | IOException e) {
                        throw new CoercingParseLiteralException("Invalid Base64 string or file write error");
                    }
                }
                throw new CoercingParseLiteralException("Expected a Base64 encoded string");
            }

            @Override
            public String serialize(@NotNull Object dataFetcherResult,
                                    @NotNull GraphQLContext graphQLContext,
                                    @NotNull Locale locale) throws CoercingSerializeException {
                if (dataFetcherResult instanceof File) {
                    try {
                        byte[] fileContent = java.nio.file.Files.readAllBytes(((File) dataFetcherResult).toPath());
                        return Base64.getEncoder().encodeToString(fileContent);
                    } catch (IOException e) {
                        throw new CoercingSerializeException("Error encoding file to Base64");
                    }
                }
                throw new CoercingSerializeException("Expected a File object");
            }

            private File createTempFile(byte[] decodedBytes) throws IOException {
                File tempFile = File.createTempFile("uploaded_", ".tmp");
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(decodedBytes);
                }
                return tempFile;
            }
        };
    }
}
