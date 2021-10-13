package io.agriledger.webserver

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.agriledger.webserver.utils.ConfigConstants
import net.corda.client.jackson.JacksonSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType.SERVLET
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.io.ResourceLoader
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import java.io.IOException

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
private open class Starter{

    internal var firebaseDatabaseUrl: String? = ConfigConstants.configData.firebaseDatabaseUrl

    @Autowired
    var resourceLoader: ResourceLoader? = null
    /**
     * Spring Bean that binds a Corda Jackson object-mapper to HTTP message types used in Spring.
     */
    @Bean
    open fun mappingJackson2HttpMessageConverter(@Autowired rpcConnection: NodeRPCConnection): MappingJackson2HttpMessageConverter {
        val mapper = JacksonSupport.createDefaultMapper(rpcConnection.proxy)
        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = mapper
        return converter
    }


    /**
     * Spring Bean that initializes FireBase App.
     */
    @Primary
    @Bean
    @Throws(IOException::class)
    open fun firebaseInit() : FirebaseApp {
        val resource = resourceLoader?.getResource(ConfigConstants.configData.firebaseClasspath)
        val serviceAccount = resource?.inputStream

        val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(firebaseDatabaseUrl)
                .build()
        return FirebaseApp.initializeApp(options)
    }
}


/**n
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    val app = SpringApplication(Starter::class.java)
    app.setBannerMode(Banner.Mode.OFF)
    app.webApplicationType = SERVLET
    app.run(*args)
}
