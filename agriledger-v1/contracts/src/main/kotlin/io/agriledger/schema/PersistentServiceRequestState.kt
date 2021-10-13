package io.agriledger.schema

import net.corda.core.schemas.PersistentState
import javax.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

/**
 * JPA Entity for saving insurance details to the database table
 */
@Entity
@Table(name = "SERVICE_REQUEST_DETAIL")
class PersistentServiceRequestState : PersistentState, Serializable {
    @Column
    var id: String = ""

    @Column
    var batchId: String? = ""

    @Column
    var createdOn: LocalDateTime? = null
    constructor()

    constructor(id: String, batchId: String?, createdOn: LocalDateTime?) {
        this.id = id
        this.batchId = batchId
        this.createdOn = createdOn
    }
}