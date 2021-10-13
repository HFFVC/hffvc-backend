package io.agriledger.webserver.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.HashMap

@Configuration
@EnableWebSecurity
open class RestSecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var objectMapper: ObjectMapper


    @Bean
    open fun tokenAuthenticationFilter(): io.agriledger.webserver.security.TokenFilter {
        return io.agriledger.webserver.security.TokenFilter()
    }

    @Bean
    open fun restAuthenticationEntryPoint(): AuthenticationEntryPoint {
        return AuthenticationEntryPoint { _, response, _ ->
            val errorObject = HashMap<String, Any>()
            val errorCode = 401
            errorObject["message"] = "Access Denied"
            errorObject["error"] = HttpStatus.UNAUTHORIZED
            errorObject["code"] = errorCode
            errorObject["timestamp"] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            response?.contentType = "application/json;charset=UTF-8"
            response?.status = errorCode
            response?.writer?.write(objectMapper.writeValueAsString(errorObject))
        }
    }


    override fun configure(http: HttpSecurity) {
        http.cors().and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().csrf()
                .disable().formLogin().disable().httpBasic().disable().exceptionHandling()
                .authenticationEntryPoint(restAuthenticationEntryPoint()).and()
                .authorizeRequests()
                .anyRequest().authenticated()
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
    }



}
