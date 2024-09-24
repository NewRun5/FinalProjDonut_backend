package com.donut.common.scalarType;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Locale;

@Configuration
public class LocalDateTimeScalarConfig {
    @Bean
    public GraphQLScalarType localDateTimeScalarType() {
        return GraphQLScalarType.newScalar()
                .name("LocalDateTime") // 1. 이름 명시. 이름은 graphql 파일에서 사용됨.
                .description("LocalDateTime Scalar") // 2. 설명. 설명은 문서화 된 타입에서 표현됨.
                .coercing(dateTimeCoercing())   // 3. 변환 과정 명시. 아래 코드에서 생성되는 객체.
                .build();
    }

    /* 변환 과정을 명시해주는 객체 */
    private Coercing<LocalDateTime, String> dateTimeCoercing() {
        return new Coercing<LocalDateTime, String>() {
            /**
             * 클라이언트 -> 서버 통신 데이터를 변환하는 과정. 문자열 -> LocalDate 로 변환됨.
             *
             * @param input          : 문자열 형태의 input 을 받아옴.
             * @param graphQLContext : 관련 컨텍스트 정보 객체. 세션, 인증 정보 등을 가져올 수 있음.
             * @param locale         : 지역 정보. 클라이언트에 따라 다르게 적용할 수 있음.
             * @return : 변환된 LocalDate 객체.
             * @throws CoercingParseValueException : input 이 String 형태가 아닐 경우 오류를 발생시킴.
             */
            @Override
            public LocalDateTime parseValue(@NotNull Object input,
                                        @NotNull GraphQLContext graphQLContext,
                                        @NotNull Locale locale) throws CoercingParseValueException {
                try {
                    if (input instanceof String) {
                        return LocalDateTime.parse((String) input);
                    }
                    throw new CoercingParseValueException("Expected a String");
                } catch (DateTimeException e) {
                    throw new CoercingParseValueException("Expected ISO 8601 time format");
                }
            }

            /**
             * 타입에서 이미 값이 명시되어 있을 경우 동작. 예를들어
             * query {
             * getEventByDate (date: "2024-09-23") {
             * event
             * }
             * }
             *
             * @param input          : 타입에서 작성되어 있던 상수.
             * @param variables      : GQL 에서 사용하는 변수 타입. 변수가 리터럴처럼 사용될 경우 사용할 수 있다. (개념 어려움)
             * @param graphQLContext : 관련 컨텍스트 정보 객체. 세션, 인증 정보 등을 가져올 수 있음.
             * @param locale         : 지역 정보. 클라이언트에 따라 다르게 적용할 수 있음.
             * @return : LocalDate 로 변환된 값.
             * @throws CoercingParseLiteralException : 상수의 포멧이 틀렸을 경우 발생되는 오류.
             */
            @Override
            public LocalDateTime parseLiteral(@NotNull Value<?> input,
                                          @NotNull CoercedVariables variables,
                                          @NotNull GraphQLContext graphQLContext,
                                          @NotNull Locale locale) throws CoercingParseLiteralException {
                if (input instanceof StringValue) {
                    try {
                        StringValue stringValue = (StringValue) input;
                        return LocalDateTime.parse(stringValue.getValue());
                    } catch (DateTimeException e) {
                        throw new CoercingParseValueException("Expected ISO 8601 time format");
                    }
                }
                throw new CoercingParseValueException("Expected a String");
            }

            /**
             * 서버 -> 클라이언트 통신 시에 데이터를 변환하는 과정.
             *
             * @param dataFetcherResult : 서버에서의 객체. 즉 LocalDate 객체. 클라이언트에 전송하기 위해 String 으로 변환 필요.
             * @param graphQLContext    : 관련 컨텍스트 정보 객체. 세션, 인증 정보 등을 가져올 수 있음.
             * @param locale            : 지역 정보. 클라이언트에 따라 다르게 적용할 수 있음.
             * @return : String 형태로 변환된 값.
             * @throws CoercingSerializeException : dataFetcherResult 의 타입이 LocalDate 가 아닐 경우 오류 발생.
             */
            @Override
            public String serialize(@NotNull Object dataFetcherResult, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingSerializeException {
                if (dataFetcherResult instanceof LocalDateTime) {
                    return ((LocalDateTime) dataFetcherResult).toString();
                }
                throw new CoercingSerializeException("Expected a LocalDate object.");
            }
        };
    }
}
