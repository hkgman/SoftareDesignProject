package com.utility.company;

import com.utility.company.dto.FacilityDto;
import com.utility.company.error.exception.FacilityNotFoundException;
import com.utility.company.model.Facility;
import com.utility.company.repository.EquipmentFacilityRepository;
import com.utility.company.repository.FacilityRepository;
import com.utility.company.service.FacilityService;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private EquipmentFacilityRepository equipmentFacilityRepository;

    @InjectMocks
    private FacilityService facilityService;

    @Test
    void shouldFindFacilityByName() {
        String name = "Facility1";
        Facility facility = new Facility(UUID.randomUUID(), name, 1000, null);
        when(facilityRepository.findOneByNameIgnoreCase(name)).thenReturn(facility);

        Facility result = facilityService.findByName(name);

        assertNotNull(result);
        assertEquals(name, result.getName());
        verify(facilityRepository).findOneByNameIgnoreCase(name);
    }

    @Test
    void shouldThrowValidationException_whenFacilityAlreadyExists() {
        String name = "Facility1";
        FacilityDto facilityDto = new FacilityDto();
        facilityDto.setName(name);
        facilityDto.setPrice(1000);

        Facility existingFacility = new Facility(UUID.randomUUID(), name, 1000, null);
        when(facilityRepository.findOneByNameIgnoreCase(name)).thenReturn(existingFacility);

        ValidationException exception = assertThrows(ValidationException.class, () -> facilityService.createFacility(facilityDto));

        assertEquals(String.format("Facility '%s' already exists", existingFacility.getName()), exception.getMessage());
        verify(facilityRepository).findOneByNameIgnoreCase(name);
    }

    @Test
    void shouldCreateFacility_whenFacilityDoesNotExist() {
        String name = "Facility1";
        FacilityDto facilityDto = new FacilityDto();
        facilityDto.setName(name);
        facilityDto.setPrice(1000);

        when(facilityRepository.findOneByNameIgnoreCase(name)).thenReturn(null);
        when(facilityRepository.save(any(Facility.class))).thenReturn(new Facility(UUID.randomUUID(), name, 1000, null));

        Facility result = facilityService.createFacility(facilityDto);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(Integer.valueOf(1000), result.getPrice());
        verify(facilityRepository).findOneByNameIgnoreCase(name);
        verify(facilityRepository).save(any(Facility.class));
    }

    @Test
    void shouldFindFacilityById() {
        UUID id = UUID.randomUUID();
        Facility facility = new Facility(id, "Facility1", 1000, null);
        when(facilityRepository.findById(id)).thenReturn(Optional.of(facility));

        Facility result = facilityService.findFacility(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(facilityRepository).findById(id);
    }

    @Test
    void shouldThrowFacilityNotFoundException_whenFacilityNotFoundById() {
        UUID id = UUID.randomUUID();
        when(facilityRepository.findById(id)).thenReturn(Optional.empty());

        FacilityNotFoundException thrown = assertThrows(FacilityNotFoundException.class, () -> facilityService.findFacility(id));
        assertTrue(thrown.getMessage().contains(id.toString()));
        verify(facilityRepository).findById(id);
    }

    @Test
    void shouldUpdateFacility() {
        UUID id = UUID.randomUUID();
        FacilityDto facilityDto = new FacilityDto();
        facilityDto.setName("Updated Facility");
        facilityDto.setPrice(2000);

        Facility existingFacility = new Facility(id, "Facility1", 1000, null);
        when(facilityRepository.findById(id)).thenReturn(Optional.of(existingFacility));
        when(facilityRepository.save(any(Facility.class))).thenReturn(new Facility(id, "Updated Facility", 2000, null));

        Facility result = facilityService.updateFacility(id, facilityDto);

        assertNotNull(result);
        assertEquals("Updated Facility", result.getName());
        assertEquals(Integer.valueOf(2000), result.getPrice());
        verify(facilityRepository).findById(id);
        verify(facilityRepository).save(any(Facility.class));
    }

    @Test
    void shouldDeleteFacility_whenFacilityIsNotUsed() {
        UUID id = UUID.randomUUID();
        when(equipmentFacilityRepository.existsByFacilityId(id)).thenReturn(false);
        Facility facility = new Facility(); // Можете задать поля объекта при необходимости
        when(facilityRepository.findById(id)).thenReturn(Optional.of(facility));
        String result = facilityService.deleteFacility(id);

        assertEquals("Услуга успешно удалена.", result);
        verify(equipmentFacilityRepository).existsByFacilityId(id);
        verify(facilityRepository).deleteById(id);
    }

    @Test
    void shouldNotDeleteFacility_whenFacilityIsUsed() {
        UUID id = UUID.randomUUID();
        when(equipmentFacilityRepository.existsByFacilityId(id)).thenReturn(true);
        Facility facility = new Facility(); // Можете задать поля объекта при необходимости
        when(facilityRepository.findById(id)).thenReturn(Optional.of(facility));
        String result = facilityService.deleteFacility(id);

        assertEquals("Услуга используется в связях с техникой. Невозможно удалить.", result);
        verify(equipmentFacilityRepository).existsByFacilityId(id);
        verify(facilityRepository, never()).deleteById(id);
    }

    @Test
    void shouldFindAllFacilities() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Facility> facilityPage = new PageImpl<>(List.of(new Facility(UUID.randomUUID(), "Facility1", 1000, null)));
        when(facilityRepository.findAll(pageable)).thenReturn(facilityPage);

        Page<Facility> result = facilityService.findAllFacilityList(pageable);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        verify(facilityRepository).findAll(pageable);
    }
}
