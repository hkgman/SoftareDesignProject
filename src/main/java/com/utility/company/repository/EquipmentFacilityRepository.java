package com.utility.company.repository;

import com.utility.company.model.EquipmentFacility;
import com.utility.company.model.EquipmentFacilityKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface EquipmentFacilityRepository extends JpaRepository<EquipmentFacility, EquipmentFacilityKey> {

    @Query("SELECT COUNT(ef) > 0 FROM EquipmentFacility ef WHERE ef.facility.id = :facilityId")
    boolean existsByFacilityId(@Param("facilityId") UUID facilityId);
}
