package io.agriledger.schema

import net.corda.core.schemas.PersistentState
import javax.persistence.*
import java.io.Serializable

/**
 * JPA Entity for saving insurance details to the database table
 */
@Entity
@Table(name = "SALES_DETAILS")
class PersistentSalesState : PersistentState, Serializable {
    @Column
    var saleId: String = ""

    @Column
    var flaggedTransaction: Boolean? = null

    constructor()

    constructor(saleId: String, flaggedTransaction: Boolean?) {
        this.saleId = saleId
        this.flaggedTransaction = flaggedTransaction
    }
}