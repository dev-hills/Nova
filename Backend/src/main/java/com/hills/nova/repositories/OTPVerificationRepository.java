package com.hills.nova.repositories;

import com.hills.nova.domain.entities.OTPVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OTPVerificationRepository extends JpaRepository<OTPVerification, UUID> {
}
