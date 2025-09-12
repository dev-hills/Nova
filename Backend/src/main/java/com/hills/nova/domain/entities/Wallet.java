package com.hills.nova.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "wallet_id")
    private UUID walletId;

    @Column(name = "wallet_number", nullable = false, unique = true, length = 20)
    private String walletNumber;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal balance = java.math.BigDecimal.ZERO;

    @Column(name = "daily_limit", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal dailyLimit = new java.math.BigDecimal("200000");

    @Column(name = "monthly_limit", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal monthlyLimit = new java.math.BigDecimal("200000");

    @Column(name = "daily_spent", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal dailySpent = java.math.BigDecimal.ZERO;

    @Column(name = "monthly_spent", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal monthlySpent = java.math.BigDecimal.ZERO;

    @Column(name = "last_daily_reset")
    private LocalDate lastDailyReset = LocalDate.now();

    @Column(name = "last_monthly_reset")
    private LocalDate lastMonthlyReset = LocalDate.now();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "sourceWallet", cascade = CascadeType.ALL)
    private List<Transaction> sentTransactions;

    @OneToMany(mappedBy = "destinationWallet", cascade = CascadeType.ALL)
    private List<Transaction> receivedTransactions;
}
