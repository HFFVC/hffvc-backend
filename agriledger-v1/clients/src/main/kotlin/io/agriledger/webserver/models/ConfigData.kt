package io.agriledger.webserver.models

data class ConfigDataSet(val environment: String,
                         val configs: List<ConfigData>
)

data class ConfigData(
        val env: String,
        val baseUrl: String,
        val sendGridKey: String,
        val firebaseClasspath: String,
        val firebaseDatabaseUrl: String,
        val ccAddresses: List<String>,
        val adminAddresses: List<String>,
        val nodeExplorerUsers: List<String>,
        val supportEmail: String,
        val supportEmailFrom: String
)