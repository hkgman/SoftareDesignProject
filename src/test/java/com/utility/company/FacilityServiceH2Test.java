package com.utility.company;

import com.utility.company.dto.FacilityDto;
import com.utility.company.error.exception.FacilityNotFoundException;
import com.utility.company.error.exception.TypeNotFoundException;
import com.utility.company.model.Facility;
import com.utility.company.repository.EquipmentFacilityRepository;
import com.utility.company.repository.FacilityRepository;
import com.utility.company.service.FacilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FacilityServiceH2Test {
    @Autowired
    private FacilityService facilityService;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private EquipmentFacilityRepository equipmentFacilityRepository;

    @BeforeEach
    void setUp() {
        facilityRepository.deleteAll();
        equipmentFacilityRepository.deleteAll();
    }

    @Test
    void testCreateFacility() {
        FacilityDto facilityDto = new FacilityDto();
        facilityDto.setName("Test Facility");
        facilityDto.setPrice(500);

        Facility savedFacility = facilityService.createFacility(facilityDto);

        assertThat(savedFacility).isNotNull();
        assertThat(savedFacility.getName()).isEqualTo("Test Facility");
        assertThat(savedFacility.getPrice()).isEqualTo(500);
    }

    @Test
    void testFindByName() {
        Facility facility = new Facility();
        facility.setName("Existing Facility");
        facility.setPrice(1000);
        facilityRepository.save(facility);

        Facility foundFacility = facilityService.findByName("Existing Facility");
        assertThat(foundFacility).isNotNull();
        assertThat(foundFacility.getName()).isEqualTo("Existing Facility");
    }

    @Test
    void testFindFacility() {
        Facility facility = new Facility();
        facility.setName("Test Facility");
        facility.setPrice(300);
        Facility savedFacility = facilityRepository.save(facility);

        Facility foundFacility = facilityService.findFacility(savedFacility.getId());
        assertThat(foundFacility).isNotNull();
        assertThat(foundFacility.getId()).isEqualTo(savedFacility.getId());
    }

    @Test
    void testUpdateFacility() {
        Facility facility = new Facility();
        facility.setName("Old Facility");
        facility.setPrice(400);
        Facility savedFacility = facilityRepository.save(facility);

        FacilityDto updateDto = new FacilityDto();
        updateDto.setName("Updated Facility");
        updateDto.setPrice(600);

        Facility updatedFacility = facilityService.updateFacility(savedFacility.getId(), updateDto);

        assertThat(updatedFacility.getName()).isEqualTo("Updated Facility");
        assertThat(updatedFacility.getPrice()).isEqualTo(600);
    }

    @Test
    void testDeleteFacility() {
        Facility facility = new Facility();
        facility.setName("To Be Deleted");
        facility.setPrice(200);
        Facility savedFacility = facilityRepository.save(facility);

        String result = facilityService.deleteFacility(savedFacility.getId());
        assertThat(result).isEqualTo("Услуга успешно удалена.");
        assertThat(facilityRepository.findById(savedFacility.getId())).isEmpty();
    }

    @Test
    void testDeleteTypeNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        assertThrows(FacilityNotFoundException.class, () -> facilityService.deleteFacility(nonExistentId));
    }

    @Test
    void testFindAllFacilityList() {
        Facility facility1 = new Facility(null, "Facility 1", 100, null);
        Facility facility2 = new Facility(null, "Facility 2", 200, null);
        facilityRepository.save(facility1);
        facilityRepository.save(facility2);

        var page = facilityService.findAllFacilityList(PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(2);
    }
}
