package com.utility.company.service;

import com.utility.company.dto.FacilityDto;
import com.utility.company.error.exception.FacilityNotFoundException;
import com.utility.company.error.exception.TypeNotFoundException;
import com.utility.company.model.Facility;
import com.utility.company.model.Type;
import com.utility.company.repository.EquipmentFacilityRepository;
import com.utility.company.repository.FacilityRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityService {
    private final FacilityRepository facilityRepository;
    private final EquipmentFacilityRepository equipmentFacilityRepository;
    @Transactional(readOnly = true)
    public Facility findByName(String name) {
        return facilityRepository.findOneByNameIgnoreCase(name);
    }
    @Transactional
    public Facility createFacility(FacilityDto facilityDto) {
        final Facility facility = findByName(facilityDto.getName());
        if (facility != null) {
            throw new ValidationException(String.format("Facility '%s' already exists", facility.getName()));
        }
        final Facility newFacility = new Facility(null, facilityDto.getName(), facilityDto.getPrice(),null);
        return facilityRepository.save(newFacility);
    }

    @Transactional(readOnly = true)
    public Facility findFacility(UUID id) {
        final Optional<Facility> base = facilityRepository.findById(id);
        return base.orElseThrow(() -> new FacilityNotFoundException(id));
    }

    @Transactional
    public Facility updateFacility(UUID id, FacilityDto facilityDto) {
        final Facility curFacility = findFacility(id);
        curFacility.setName(facilityDto.getName());
        curFacility.setPrice(facilityDto.getPrice());
        return facilityRepository.save(curFacility);
    }

    @Transactional
    public String deleteFacility(UUID facilityId) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException(facilityId));
        boolean isUsed = equipmentFacilityRepository.existsByFacilityId(facilityId);

        if (isUsed) {
            return "Услуга используется в связях с техникой. Невозможно удалить.";
        }

        facilityRepository.deleteById(facilityId);
        return "Услуга успешно удалена.";
    }
    @Transactional(readOnly = true)
    public Page<Facility> findAllFacilityList(Pageable pageable) {
        return facilityRepository.findAll(pageable);
    }

}
