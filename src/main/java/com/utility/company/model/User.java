package com.utility.company.model;

import com.utility.company.model.enums.UserRole;
/*import com.utility.company.model.UserAddress;*/
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    @Size(min = 7, max = 64)
    private String email;

    @Column(nullable = false, length = 64)
    @Size(min = 8, max = 64)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false, length = 64)
    @Size(min = 8, max = 64)
    private String fullName;

    @Column(nullable = false, unique = true, length = 16)
    @Size(min = 16, max = 16)
    private String phone;

    @Column(nullable = false)
    private LocalDate birthday;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade =
            {
                    CascadeType.REMOVE,
                    CascadeType.MERGE,
                    CascadeType.PERSIST
            }, orphanRemoval = true)
    private List<Equipment> equipments;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade =
            {
                    CascadeType.REMOVE,
                    CascadeType.MERGE,
                    CascadeType.PERSIST
            }, orphanRemoval = true)
    private List<UserNotification> notifications;

    public void removeNotification(UserNotification userNotification){
        if (notifications.contains(userNotification))
            this.notifications.remove(userNotification);
    }
}
