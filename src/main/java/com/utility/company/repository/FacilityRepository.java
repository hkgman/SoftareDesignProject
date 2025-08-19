package com.utility.company.repository;

import com.utility.company.model.Facility;
import com.utility.company.model.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FacilityRepository extends JpaRepository<Facility, UUID> {
    Facility findOneByNameIgnoreCase(String name);

    Page<Facility> findAll(Pageable pageable);
    @Query("SELECT f FROM Facility f JOIN f.equipments tf WHERE tf.equipment.id = :equipmentId")
    List<Facility> findFacilitiesByEquipmentId(@Param("equipmentId") UUID equipmentId);
}
