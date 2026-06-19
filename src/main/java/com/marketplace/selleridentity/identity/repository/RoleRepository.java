package com.marketplace.selleridentity.identity.repository;

import com.marketplace.selleridentity.identity.entity.Role;
import com.marketplace.selleridentity.identity.entity.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleType name);
}
