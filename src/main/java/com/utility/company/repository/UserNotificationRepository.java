package com.utility.company.repository;

import com.utility.company.model.User;
import com.utility.company.model.UserNotification;
import com.utility.company.model.UserNotificationKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserNotificationRepository extends JpaRepository<UserNotification, UserNotificationKey> {
    Page<UserNotification> findByUserOrderByIsChecked(User user, Pageable pageable);

    List<UserNotification> findByUser(User user);

    Long countByUserAndIsCheckedFalse(User user);
}
