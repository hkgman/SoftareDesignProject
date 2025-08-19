package com.utility.company;

import com.utility.company.dto.EquipmentDto;
import com.utility.company.model.Equipment;
import com.utility.company.model.Type;
import com.utility.company.model.User;
import com.utility.company.model.enums.UserRole;
import com.utility.company.repository.EquipmentRepository;
import com.utility.company.repository.TypeRepository;
import com.utility.company.repository.UserRepository;
import com.utility.company.service.EquipmentService;
import com.utility.company.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import com.utility.company.dto.EquipmentDto;
import com.utility.company.error.exception.EquipmentNotFoundException;
import com.utility.company.model.*;
import com.utility.company.model.enums.Status;
import com.utility.company.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserService userService;

    @Mock
    private TypeRepository typeRepository;

    @Mock
    private EquipmentFacilityRepository equipmentFacilityRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    private Equipment equipment;
    private EquipmentDto equipmentDto;
    private User user;
    private Type type;

    @BeforeEach
    public void setUp() {
        user = new User(UUID.randomUUID(), "test@example.com", "password123", UserRole.USER, "Test User", "1234567890123456", LocalDate.now(), null, null);
        type = new Type(UUID.randomUUID(), "Excavator");
        equipment = new Equipment(UUID.randomUUID(), "Excavator-1", LocalDate.now(), type, Status.CREATE, user, null);
        equipmentDto = new EquipmentDto();
        equipmentDto.setName("Excavator-2");
        equipmentDto.setEquipmentType(type.getId());
    }

    @Test
    public void testAddEquipment() {
        when(userService.findByPrincipal()).thenReturn(user);
        when(typeRepository.findById(equipmentDto.getEquipmentType())).thenReturn(Optional.of(type));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);

        Equipment result = equipmentService.addEquipment(equipmentDto);

        verify(equipmentRepository).save(any(Equipment.class));
        verify(userService).sendNotificationAboutEquipments(any(), eq(user.getId()));
    }

    @Test
    public void testFindEquipment() {
        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));

        Equipment result = equipmentService.find(equipment.getId());

        assert result != null;
        verify(equipmentRepository).findById(equipment.getId());
    }

    @Test
    public void testFindEquipmentThrowsException() {
        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.empty());

        try {
            equipmentService.find(equipment.getId());
        } catch (EquipmentNotFoundException e) {
            assert e.getMessage().contains(equipment.getId().toString());
        }
    }

    @Test
    public void testGetAllEquipments() {
        Page<Equipment> equipmentPage = new PageImpl<>(List.of(equipment));
        when(equipmentRepository.findAll(any(Pageable.class))).thenReturn(equipmentPage);

        Page<Equipment> result = equipmentService.getAllEquipments(Pageable.unpaged());

        assert result != null;
        verify(equipmentRepository).findAll(any(Pageable.class));
    }

    @Test
    public void testUpdateEquipment() {
        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));
        when(typeRepository.findById(equipmentDto.getEquipmentType())).thenReturn(Optional.of(type));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);

        Equipment result = equipmentService.updateEquipment(equipment.getId(), equipmentDto);

        assert result.getName().equals(equipmentDto.getName());
        verify(equipmentRepository).save(any(Equipment.class));
    }

    @Test
    public void testUpdateStatus() {
        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));

        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);

        Equipment result = equipmentService.updateStatus(equipment.getId(), "false");

        assertEquals(Status.SEND, result.getStatus());

        verify(equipmentRepository).save(any(Equipment.class));
    }

    @Test
    public void testDeleteEquipment() {
        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));

        equipmentService.delete(equipment.getId());

        verify(equipmentRepository).deleteEquipmentFacilities(equipment.getId());
        verify(equipmentRepository).deleteEquipment(equipment.getId());
    }
}
