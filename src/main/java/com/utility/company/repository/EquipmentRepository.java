package com.utility.company.repository;

import com.utility.company.dto.EquipmentReportEqDto;
import com.utility.company.model.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {
    @Query("SELECT e FROM Equipment e WHERE e.user.id = :userId")
    Page<Equipment> findByUserId(@Param("userId") UUID userId, Pageable pageable);


    Page<Equipment> findAll(Pageable pageable);

    @Query("SELECT COUNT(e) > 0 FROM Equipment e WHERE e.equipmentType.id = :typeId")
    boolean existsByTypeId(@Param("typeId") UUID typeId);
    @Modifying
    @Transactional
    @Query("DELETE FROM Equipment e WHERE e.id = :id")
    void deleteEquipment(UUID id);

    @Modifying
    @Transactional
    @Query("DELETE FROM EquipmentFacility ef WHERE ef.equipment.id = :id")
    void deleteEquipmentFacilities(UUID id);

    @Query("SELECT new com.utility.company.dto.EquipmentReportEqDto(e.id, e.name, et.text, u.fullName) " +
            "FROM Equipment e " +
            "JOIN e.equipmentType et " +
            "LEFT JOIN e.user u " +
            "WHERE e.status = 'PAY'")
    List<EquipmentReportEqDto> findEquipmentData();

    @Query("SELECT new com.utility.company.dto.EquipmentReportEqDto(e.id, e.name, et.text, u.fullName) " +
            "FROM Equipment e " +
            "JOIN e.equipmentType et " +
            "LEFT JOIN e.user u " +
            "WHERE e.status = 'PAY' and e.id = :id")
    EquipmentReportEqDto findEquipmentReport(UUID id);

    @Query("SELECT ef.equipment.id, f.name, f.price FROM EquipmentFacility ef " +
            "JOIN ef.facility f WHERE ef.equipment.id IN :equipmentIds")
    List<Object[]> findFacilitiesByEquipmentIds(@Param("equipmentIds") List<UUID> equipmentIds);
}
