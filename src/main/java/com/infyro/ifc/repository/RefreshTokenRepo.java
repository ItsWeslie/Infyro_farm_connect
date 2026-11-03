package com.infyro.ifc.repository;

import com.infyro.ifc.model.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshToken t join fetch t.user where t.tokenHash = :hash")
    Optional<RefreshToken> findForUpdateByTokenHash(@Param("hash") String hash);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now where t.familyId = :family and t.revokedAt is null")
    int revokeFamily(@Param("family") String family, @Param("now") Instant now);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now where t.user.id = :userId and t.revokedAt is null")
    int revokeAllForUser(@Param("userId") Long userId, @Param("now") Instant now);

    boolean existsByFamilyIdAndRevokedAtIsNullAndExpiresAtAfter(String familyId, Instant now);

    List<RefreshToken> findAllByFamilyId(String familyId);
}