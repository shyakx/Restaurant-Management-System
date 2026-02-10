package com.restaurant.kitchen.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "kitchen_orders")
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
    val totalAmount: java.math.BigDecimal,
    
    @OneToMany(mappedBy = "kitchenOrder", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var items: List<KitchenOrderItem> = emptyList(),
    
    @Column(nullable = false)
    val receivedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    var startedPreparationAt: LocalDateTime? = null,
    
    @Column
    var completedAt: LocalDateTime? = null,
    
    @Column
    var estimatedCompletionTime: LocalDateTime? = null,
    
    @Column
    val assignedTo: String? = null
)

enum class KitchenOrderStatus {
    RECEIVED,
    IN_PREPARATION,
    READY,
    COMPLETED,
    CANCELLED
}
