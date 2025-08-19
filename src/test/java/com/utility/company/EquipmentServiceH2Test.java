package com.utility.company;

import com.utility.company.dto.EquipmentDto;
import com.utility.company.error.exception.EquipmentNotFoundException;
import com.utility.company.error.exception.TypeNotFoundException;
import com.utility.company.model.*;
import com.utility.company.model.enums.Status;
import com.utility.company.model.enums.UserRole;
import com.utility.company.repository.*;
import com.utility.company.service.EquipmentService;
import com.utility.company.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class EquipmentServiceH2Test {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private TypeRepository typeRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private EquipmentFacilityRepository equipmentFacilityRepository;

    @MockBean
    private UserService userService;

    @Autowired
    UserRepository userRepository;

    private User user;
    @BeforeEach
    void setUp() {
        equipmentRepository.deleteAll();
        typeRepository.deleteAll();
        facilityRepository.deleteAll();
        equipmentFacilityRepository.deleteAll();
        user = new User();
        user.setEmail("test.user@example.com");
        user.setPassword("securePassword");
        user.setRole(UserRole.USER);
        user.setFullName("Test User");
        user.setPhone("1234567812345678");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userRepository.save(user);
    }

    @Test
    @Transactional
    void testAddEquipment() {
        Type type = new Type();
        type.setText("Test Type");
        typeRepository.save(type);

        EquipmentDto equipmentDto = new EquipmentDto();
        equipmentDto.setName("Test Equipment");
        equipmentDto.setEquipmentType(type.getId());
        when(userService.findByPrincipal()).thenReturn(user);
        Equipment savedEquipment = equipmentService.addEquipment(equipmentDto);

        assertThat(savedEquipment).isNotNull();
        assertThat(savedEquipment.getName()).isEqualTo("Test Equipment");
        assertThat(savedEquipment.getEquipmentType().getId()).isEqualTo(type.getId());
    }

    @Test
    @Transactional
    void testFindById() {
        Type type = new Type();
        type.setText("Test Type");
        typeRepository.save(type);

        Equipment equipment = new Equipment(null, "Test Equipment", LocalDate.now(), type, Status.CREATE, user, null);
        Equipment savedEquipment = equipmentRepository.save(equipment);

        Equipment foundEquipmentDto = equipmentService.find(savedEquipment.getId());

        assertThat(foundEquipmentDto).isNotNull();
        assertThat(foundEquipmentDto.getId()).isEqualTo(savedEquipment.getId());
    }

    @Test
    @Transactional
    void testUpdateEquipment() {
        Type type = new Type();
        type.setText("Old Type");
        typeRepository.save(type);

        Equipment equipment = new Equipment(null, "Old Equipment", LocalDate.now(), type, Status.CREATE, user, null);
        Equipment savedEquipment = equipmentRepository.save(equipment);

        Type newType = new Type();
        newType.setText("Updated Type");
        typeRepository.save(newType);

        EquipmentDto updateDto = new EquipmentDto();
        updateDto.setName("Updated Equipment");
        updateDto.setEquipmentType(newType.getId());

        Equipment updatedEquipment = equipmentService.updateEquipment(savedEquipment.getId(), updateDto);

        assertThat(updatedEquipment.getName()).isEqualTo("Updated Equipment");
        assertThat(updatedEquipment.getEquipmentType().getId()).isEqualTo(newType.getId());
    }

    @Test
    void testDeleteEquipment() {
        Type type = new Type();
        type.setText("Test Type");
        typeRepository.save(type);

        Equipment equipment = new Equipment(null, "Test Equipment", LocalDate.now(), type, Status.CREATE, user, null);
        Equipment savedEquipment = equipmentRepository.save(equipment);

        Equipment deletedEquipmentDto = equipmentService.delete(savedEquipment.getId());
        assertThat(deletedEquipmentDto).isNotNull();
        assertThat(equipmentRepository.findById(savedEquipment.getId())).isEmpty();
    }

    @Test
    @Transactional
    void testDeleteEquipmentNotFound()
    {
        UUID nonExistentId = UUID.randomUUID();
        assertThrows(EquipmentNotFoundException.class, () -> equipmentService.delete(nonExistentId));
    }

    @Test
    @Transactional
    void testAddFacilityToEquipment() {
        Type type = new Type();
        type.setText("Test Type");
        typeRepository.save(type);

        Equipment equipment = new Equipment(null, "Test Equipment", LocalDate.now(), type, Status.CREATE, user, new ArrayList<>());
        Equipment savedEquipment = equipmentRepository.save(equipment);

        Facility facility = new Facility();
        facility.setName("Test Facility");
        facility.setPrice(1000);
        facility.setEquipments(new ArrayList<>());
        facilityRepository.save(facility);
        equipmentService.addFacilityToEquipment(savedEquipment.getId(), facility.getId());

        EquipmentFacility equipmentFacility = equipmentFacilityRepository.findAll().get(0);
        assertThat(equipmentFacility).isNotNull();
        assertThat(equipmentFacility.getEquipment().getId()).isEqualTo(savedEquipment.getId());
        assertThat(equipmentFacility.getFacility().getId()).isEqualTo(facility.getId());
    }

    @Test
    @Transactional
    void testRemoveFacilityFromEquipment() {
        Type type = new Type();
        type.setText("Test Type");
        typeRepository.save(type);

        Equipment equipment = new Equipment(null, "Test Equipment", LocalDate.now(), type, Status.CREATE, user, new ArrayList<>());
        Equipment savedEquipment = equipmentRepository.save(equipment);

        Facility facility = new Facility();
        facility.setName("Test Facility");
        facility.setPrice(1000);
        facility.setEquipments(new ArrayList<>());
        facilityRepository.save(facility);
        equipmentService.addFacilityToEquipment(savedEquipment.getId(), facility.getId());

        EquipmentFacility equipmentFacility = equipmentFacilityRepository.findAll().get(0);

        assertThat(savedEquipment.getFacilities()).hasSize(1);
        assertThat(savedEquipment.getFacilities().get(0).getFacility()).isEqualTo(facility);

        equipmentService.removeFacilityFromEquipment(savedEquipment.getId(), facility.getId());

        assertThat(equipmentFacilityRepository.findAll()).isEmpty();
        assertThat(equipmentRepository.findById(savedEquipment.getId()).get().getFacilities()).isEmpty();
        assertThat(facilityRepository.findById(facility.getId()).get().getEquipments()).isEmpty();
    }

    @Test
    @Transactional
    void testGetAllEquipments() {
        Type type = new Type();
        type.setText("Test Type");
        typeRepository.save(type);

        Equipment equipment1 = new Equipment(null, "Test Equipment 1", LocalDate.now(), type, Status.CREATE, user, null);
        Equipment equipment2 = new Equipment(null, "Test Equipment 2", LocalDate.now(), type, Status.CREATE, user, null);
        equipmentRepository.save(equipment1);
        equipmentRepository.save(equipment2);

        Page<Equipment> equipmentPage = equipmentService.getAllEquipments(PageRequest.of(0, 10));

        assertThat(equipmentPage.getTotalElements()).isEqualTo(2);
    }

    @Test
    @Transactional
    void testUpdateStatus() {
        Type type = new Type();
        type.setText("Test Type");
        typeRepository.save(type);

        Equipment equipment = new Equipment(null, "Test Equipment", LocalDate.now(), type, Status.SEND, user, null);
        Equipment savedEquipment = equipmentRepository.save(equipment);

        Equipment updatedEquipmentDto = equipmentService.updateStatus(savedEquipment.getId(), "true");

        assertThat(updatedEquipmentDto.getStatus()).isEqualTo(Status.IN_WORK);
    }
}


