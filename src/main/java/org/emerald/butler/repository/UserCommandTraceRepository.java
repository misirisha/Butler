package org.emerald.butler.repository;

import java.util.Optional;

import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.UserCommand;
import org.emerald.butler.entity.UserCommandTrace;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCommandTraceRepository extends JmixDataRepository<UserCommandTrace, String> {
    Optional<UserCommandTrace> findFirstByUserCommandOrderByOrderDesc(UserCommand userCommand);
}
