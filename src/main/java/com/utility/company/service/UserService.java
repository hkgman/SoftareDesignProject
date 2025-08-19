package com.utility.company.service;

import com.utility.company.dto.UserDto;
import com.utility.company.dto.UserSignUpDto;
import com.utility.company.error.exception.UNotificationNotFoundException;
import com.utility.company.error.exception.UserNotFoundException;
import com.utility.company.model.*;
import com.utility.company.model.enums.UserRole;
import com.utility.company.repository.UserRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService extends DefaultOAuth2UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private final NotificationService notificationService;

    @Value("{spring.mail.username}")
    private String emailFrom;

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findOneByEmailIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public User findByPrincipal() {
        User user = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof DefaultOAuth2User userDetails) {
            user = findByEmail(userDetails.getAttribute("email"));
        } else if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            user = findByEmail(userDetails.getUsername());
        }
        if (user == null) {
            throw new UserNotFoundException(null);
        }
        return user;
    }

    @Transactional(readOnly = true)
    public Page<UserNotification> findNotificationsOrderByIsChecked(Pageable pageable) {
        return notificationService.findByUserOrderByIsChecked(findByPrincipal(), pageable);
    }

    @Transactional
    public User create(UserSignUpDto userSignupDto) {
        final User user = findByEmail(userSignupDto.getEmail());
        if (user != null) {
            throw new ValidationException(String.format("User '%s' already exists", user.getEmail()));
        }
        if (userRepository.findUserByPhone(userSignupDto.getPhone()) != null) {
            throw new ValidationException(String.format("User with phone number '%s' already exists", userSignupDto.getPhone()));
        }
        if (!Objects.equals(userSignupDto.getPassword(), userSignupDto.getPasswordConfirm())) {
            throw new ValidationException("Passwords not equals");
        }
        UserRole role;
        LocalDate minimumBirthday = LocalDate.now().minusYears(18);
        if (userSignupDto.getEmail().equals("sorokin.zxcv@gmail.com")) {
            role=UserRole.ADMIN;
        } else {
            role = UserRole.USER;
        }
        final User newUser = new User(null, userSignupDto.getEmail(), passwordEncoder.encode(userSignupDto.getPassword()),
                role, userSignupDto.getFullName(), userSignupDto.getPhone(), userSignupDto.getBirthday(),
                 null,null);
        return userRepository.save(newUser);
    }

    @Transactional
    public void callRepositorySave(User user) {
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User find(UUID id) {
        final Optional<User> user = userRepository.findById(id);
        return user.orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public User update(UUID id, UserDto userDto) {
        final User currentUser = find(id);
        final User sameUser = findByEmail(userDto.getEmail());
        if (sameUser != null && !Objects.equals(sameUser.getId(), currentUser.getId())) {
            throw new ValidationException(String.format("User '%s' already exists", userDto.getEmail()));
        }
        final User sameUserPhone = userRepository.findUserByPhone(userDto.getPhone());
        if (sameUserPhone != null && !Objects.equals(sameUserPhone.getId(), currentUser.getId())) {
            throw new ValidationException(String.format("User with phone number '%s' already exists", userDto.getPhone()));
        }
        currentUser.setEmail(userDto.getEmail());
        currentUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        currentUser.setFullName(userDto.getFullName());
        currentUser.setPhone(userDto.getPhone());
        return userRepository.save(currentUser);
    }


    @Transactional
    public Boolean deleteNotificationsInUser(String email, List<UUID> notificationIds) {
        final User curUser = findByEmail(email);
        for (UUID notificationId : notificationIds) {
            notificationService.findByUUID(notificationId);
            final UserNotification curUserNotification = userRepository.findUserNotification(curUser.getId(), notificationId);
            if (curUserNotification == null)
                throw new UNotificationNotFoundException(curUser.getId());
            curUser.getNotifications().remove(curUserNotification);
        }
        userRepository.save(curUser);
        return notificationService.hasUnreadNotifications(curUser) > 0L;
    }

    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User userEntity = findByEmail(username);
        if (userEntity == null) {
            throw new UsernameNotFoundException(username);
        }
        return new org.springframework.security.core.userdetails.User(
                userEntity.getEmail(), userEntity.getPassword(), Collections.singleton(userEntity.getRole()));
    }

    @Transactional
    public List<UUID> getAdmins()
    {
        return userRepository.findUserIdsByRole(UserRole.ADMIN);
    }



    @Transactional
    public void sendNotificationAboutEquipments(String text,UUID userId) {
        User user = find(userId);
        Notification notification = new Notification();
        notification.setText(text);
        notificationService.save(notification);
        Notification notification1 = notificationService.findByUUID(notification.getId());

        UserNotification userNotification = new UserNotification();
        userNotification.setId(new UserNotificationKey(user.getId(),notification1.getId()));
        userNotification.setNotification(notification1);
        userNotification.setChecked(false);
        userNotification.setUser(user);
        userNotification.setNotification(notification1);
        notificationService.save(userNotification);
    }
}
