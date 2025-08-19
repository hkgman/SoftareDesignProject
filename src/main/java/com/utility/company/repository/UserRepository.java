package com.utility.company.repository;

import com.utility.company.model.User;
import com.utility.company.model.UserNotification;
import com.utility.company.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findOneByEmailIgnoreCase(String email);

    User findUserByPhone(String phone);

    @Query("Select un from UserNotification un where un.user.id = :userId and un.notification.id = :notificationId")
    UserNotification findUserNotification(@Param("userId") UUID userId, @Param("notificationId") UUID notificationId);

    @Query("SELECT u.id FROM users u WHERE u.role = :role")
    List<UUID> findUserIdsByRole(UserRole role);
}
