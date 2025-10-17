package com.pipemasters.server.repository;

import com.pipemasters.server.entity.UserAccount;
import java.util.Optional;

public interface UserAccountRepository extends GeneralRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
}
