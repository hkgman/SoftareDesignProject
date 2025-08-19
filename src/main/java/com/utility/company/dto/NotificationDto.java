package com.utility.company.dto;

import com.utility.company.model.Notification;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class NotificationDto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,length = 1024)
    @Size(min=8)
    private String text;

    public NotificationDto(Notification notification) {
        this.id = notification.getId();
        this.text = notification.getText();
    }
}
