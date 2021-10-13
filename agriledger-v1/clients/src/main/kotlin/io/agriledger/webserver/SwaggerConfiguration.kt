package io.agriledger.webserver

import io.swagger.models.auth.In
import org.springframework.context.annotation.Configuration
import springfox.documentation.swagger2.annotations.EnableSwagger2
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import springfox.documentation.service.ApiKey


@Configuration
@EnableSwagger2
open class SwaggerConfiguration {

    @Bean
    open fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(mutableListOf(apiKey()))
    }

    private fun apiKey(): ApiKey {
        return ApiKey("Token Access", HttpHeaders.AUTHORIZATION, In.HEADER.name)
    }
}