package com.restaurant.kitchen.entity

import jakarta.persistence.*
import java.math.BigDecimal
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

@Entity
@Table(name = "kitchen_orders")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "orderId"
)
data class KitchenOrder(
    @Id
    val orderId: Long,
    
    @Column(nullable = false)
    val customerName: String,
    
    @Column(nullable = false)
    val customerEmail: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: KitchenOrderStatus = KitchenOrderStatus.RECEIVED,
    
    @Column(nullable = false)
    val totalAmount: BigDecimal,
    
    @OneToMany(mappedBy = "kitchenOrder", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var items: MutableList<KitchenOrderItem> = mutableListOf(),
    
    @Column(nullable = false)
    val receivedAt: String = java.time.LocalDateTime.now().toString(),
    
    @Column
    var startedPreparationAt: String? = null,
    
    @Column
    var completedAt: String? = null,
    
    @Column
    var estimatedCompletionTime: String? = null,
    
    @Column
    val assignedTo: String? = null
) {
    // Default constructor for Hibernate
    constructor() : this(
        orderId = 0L,
        customerName = "",
        customerEmail = "",
        status = KitchenOrderStatus.RECEIVED,
        totalAmount = BigDecimal.ZERO,
        items = mutableListOf(),
        receivedAt = "",
        startedPreparationAt = null,
        completedAt = null,
        estimatedCompletionTime = null,
        assignedTo = null
    )
}

enum class KitchenOrderStatus {
    RECEIVED,
    IN_PREPARATION,
    READY,
    COMPLETED,
    CANCELLED
}
