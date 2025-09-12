package com.hills.nova.domain.entities;

import com.hills.nova.domain.enums.TransactionStatus;
import com.hills.nova.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "transaction_reference", nullable = false, unique = true, length = 50)
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal amount;

    @Column(name = "fee", precision = 15, scale = 2)
    private java.math.BigDecimal fee = java.math.BigDecimal.ZERO;

    @Column(name = "narration", length = 500)
    private String narration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

//    @Column(name = "external_reference", length = 100)
//    private String externalReference;

//    @Column(name = "channel", length = 50)
//    private String channel = "MOBILE_APP";

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "reversed", nullable = false)
    private Boolean reversed = false;

    @Column(name = "reversal_reference", length = 50)
    private String reversalReference;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "source_wallet_id")
    private Wallet sourceWallet;

    @ManyToOne
    @JoinColumn(name = "destination_wallet_id")
    private Wallet destinationWallet;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
