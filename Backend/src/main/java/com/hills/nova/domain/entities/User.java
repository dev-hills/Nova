    package com.hills.nova.domain.entities;

    import com.hills.nova.domain.enums.IdType;
    import jakarta.persistence.*;
    import jakarta.validation.constraints.Pattern;
    import jakarta.validation.constraints.Size;
    import lombok.*;
    import org.hibernate.annotations.CreationTimestamp;
    import org.hibernate.annotations.UpdateTimestamp;

    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.UUID;

    @Entity
    @Table(name = "users")
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "user_id")
        private UUID id;

        @Column(name = "first_name", nullable = false, length = 100)
        private String firstName;

        @Column(name = "last_name", nullable = false, length = 100)
        private String lastName;

        @Column(name = "date_of_birth", nullable = false)
        private LocalDate dateOfBirth;

        @Column(name = "mobile_number", nullable = false, unique = true, length = 11)
        private String mobileNumber;

        @Column(name = "email", nullable = false, unique = true, length = 255)
        private String email;

        @Column(name = "avatar_url")
        private String avatarUrl;

        //TODO: HASH PASSWORD
        @Column(name = "password", nullable = false, length = 255)
        private String password;

        //TODO: HASH PIN
        @Column(name = "transaction_pin", length = 6)
        @Size(min = 6, max = 6, message = "PIN must be exactly 6 digits")
        @Pattern(regexp = "\\d{6}", message = "PIN must be numeric")
        private String transactionPin;

        @Column(name = "verified_at")
        private LocalDateTime verifiedAt;

        @Builder.Default
        @Column(name = "is_verified", nullable = false)
        private Boolean isVerified = false;

        @CreationTimestamp
        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
        private Wallet wallet;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
        private List<OTPVerification> otpVerifications;

        //    @Column(name = "mobile_verified", nullable = false)
        //    private Boolean mobileVerified = false;

        //    @Enumerated(EnumType.STRING)
        //    @Column(name = "id_type", nullable = false)
        //    private IdType idType;

        //    @Column(name = "id_number", nullable = false, unique = true, length = 50)
        //    private String idNumber;

        //    @Column(name = "selfie_url", length = 500)
        //    private String selfieUrl;

        //    @Column(name = "id_card_url", length = 500)
        //    private String idCardUrl;`
    }
