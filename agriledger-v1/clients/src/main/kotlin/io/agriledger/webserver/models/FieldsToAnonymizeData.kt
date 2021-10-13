package io.agriledger.webserver.models

data class FieldsToAnonymizeDataSet(
        val fields: List<FieldsToAnonymizeData>
)

data class FieldsToAnonymizeData(val state: String,
                                 val fieldsToAnonymize: List<FieldsToAnonymize>
)