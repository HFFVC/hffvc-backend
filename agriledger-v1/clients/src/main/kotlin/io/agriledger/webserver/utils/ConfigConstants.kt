package io.agriledger.webserver.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.agriledger.webserver.models.ConfigDataSet

object ConfigConstants {
    val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())
    val dataSet: ConfigDataSet = mapper.readValue(Thread.currentThread().contextClassLoader.getResourceAsStream("settings.json"))
    val configData = dataSet.configs.single{ it.env == dataSet.environment}
}