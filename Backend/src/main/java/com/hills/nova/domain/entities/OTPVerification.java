package com.hills.nova.domain.entities;

import com.hills.nova.domain.enums.OTPDeliveryMethod;
import com.hills.nova.domain.enums.OTPPurpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp_verifications")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OTPVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "otp_id")
    private UUID otpId;

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;


    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private OTPPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", nullable = false)
    private OTPDeliveryMethod deliveryMethod = OTPDeliveryMethod.SMS;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 3;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
