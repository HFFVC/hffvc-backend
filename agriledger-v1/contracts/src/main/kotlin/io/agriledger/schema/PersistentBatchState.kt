package io.agriledger.schema

import net.corda.core.schemas.PersistentState
import javax.persistence.*
import java.io.Serializable

/**
 * JPA Entity for saving insurance details to the database table
 */
@Entity
@Table(name = "BATCH_DETAILS")
class PersistentBatchState : PersistentState, Serializable {
    @Column
    var batchId: String = ""

    constructor()

    constructor(batchId: String) {
        this.batchId = batchId
    }
}