package com.utility.company.service;

import com.utility.company.dto.EquipmentDto;
import com.utility.company.dto.EquipmentReportDto;
import com.utility.company.dto.EquipmentReportEqDto;
import com.utility.company.error.exception.EFacilityNotFoundException;
import com.utility.company.error.exception.EquipmentNotFoundException;
import com.utility.company.error.exception.FacilityNotFoundException;
import com.utility.company.error.exception.TypeNotFoundException;
import com.utility.company.model.*;
import com.utility.company.model.enums.Status;
import com.utility.company.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    private final UserService userService;

    private final TypeRepository typeRepository;

    private final EquipmentFacilityRepository equipmentFacilityRepository;

    private final FacilityRepository facilityRepository;
    @Transactional
    public Equipment addEquipment(EquipmentDto equipmentDto) {
        User user = userService.findByPrincipal();

        Type type = typeRepository.findById(equipmentDto.getEquipmentType())
                .orElseThrow(() -> new TypeNotFoundException(equipmentDto.getEquipmentType()));

        Equipment equipment = new Equipment(null,equipmentDto.getName(),LocalDate.now(),type,Status.CREATE,user,null);// Статус подставляется автоматически
        userService.sendNotificationAboutEquipments("Была создана техника " + equipmentDto.getName() +" с типом:" + type.getText(),user.getId());
        return equipmentRepository.save(equipment);
    }
    @Transactional
    public List<EquipmentFacility> getFacilitiesByEquipment(UUID typeId) {
        Equipment equipment = find(typeId);
        return equipment.getFacilities();
    }
    @Transactional
    public Equipment pay(UUID id)
    {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new EquipmentNotFoundException(id));
        if(equipment.getStatus() == Status.READY){
            equipment.setStatus(Status.PAY);
        }
        List<UUID> adminsId = userService.getAdmins();
        for (UUID adminId : adminsId){
            log.info(adminId.toString());
            userService.sendNotificationAboutEquipments("Ремонт техники " + equipment.getName() + " был оплачен",adminId);
        }
        userService.sendNotificationAboutEquipments("Ремонт техники " + equipment.getName() + " был успешно оплачен! Спасибо, что выбрали нас",equipment.getUser().getId());
        return equipmentRepository.save(equipment);

    }
    @Transactional(readOnly = true)
    public Equipment find(UUID id) {
        final Optional<Equipment> equipmentOptional = equipmentRepository.findById(id);
        return equipmentOptional.orElseThrow(() -> new EquipmentNotFoundException(id));
    }
    @Transactional
    public Page<Equipment> getEquipmentByUser(Pageable pageable) {
        User user = userService.findByPrincipal();
        return equipmentRepository.findByUserId(user.getId(),pageable);
    }
    @Transactional
    public Page<Equipment> getAllEquipments(Pageable pageable){
        return equipmentRepository.findAll(pageable);
    }

    public List<EquipmentReportDto> getEquipmentReport() {
        List<EquipmentReportEqDto> equipmentList = equipmentRepository.findEquipmentData();

        List<UUID> equipmentIds = equipmentList.stream()
                .map(EquipmentReportEqDto::getEquipmentId)
                .collect(Collectors.toList());

        List<Object[]> facilitiesData = equipmentRepository.findFacilitiesByEquipmentIds(equipmentIds);

        Map<UUID, List<EquipmentReportDto.FacilityInfo>> facilitiesMap = facilitiesData.stream()
                .collect(Collectors.groupingBy(
                        data -> (UUID) data[0],
                        Collectors.mapping(data -> new EquipmentReportDto.FacilityInfo((String) data[1], (Integer) data[2]), Collectors.toList())
                ));

        return equipmentList.stream()
                .map(equipment -> new EquipmentReportDto(
                        equipment.getEquipmentName(),
                        equipment.getEquipmentType(),
                        equipment.getUserName(),
                        facilitiesMap.getOrDefault(equipment.getEquipmentId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    public EquipmentReportDto getEquipmentOneReport(UUID id) {
        EquipmentReportEqDto equipmentReportDto = equipmentRepository.findEquipmentReport(id);
        List<UUID> ids = new ArrayList<>();
        ids.add(id);
        List<Object[]> facilitiesData = equipmentRepository.findFacilitiesByEquipmentIds(ids);

        Map<UUID, List<EquipmentReportDto.FacilityInfo>> facilitiesMap = facilitiesData.stream()
                .collect(Collectors.groupingBy(
                        data -> (UUID) data[0],
                        Collectors.mapping(data -> new EquipmentReportDto.FacilityInfo((String) data[1], (Integer) data[2]), Collectors.toList())
                ));

        EquipmentReportDto eq = new EquipmentReportDto();
        eq.setEquipmentName(equipmentReportDto.getEquipmentName());
        eq.setEquipmentType(equipmentReportDto.getEquipmentType());
        eq.setUserName(equipmentReportDto.getUserName());
        eq.setFacilities(facilitiesMap.getOrDefault(id, Collections.emptyList()));
        return eq;
    }
    @Transactional
    public Equipment updateEquipment(UUID equipmentId, EquipmentDto equipmentDto) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));

        Type type = typeRepository.findById(equipmentDto.getEquipmentType())
                .orElseThrow(() -> new TypeNotFoundException(equipmentDto.getEquipmentType()));
        userService.sendNotificationAboutEquipments("Была обновлена техника " + equipment.getName() + " " + equipment.getEquipmentType().getText() + " -> " + equipmentDto.getName() + " " +type.getText(),equipment.getUser().getId());
        equipment.setName(equipmentDto.getName());
        equipment.setEquipmentType(type);

        return equipmentRepository.save(equipment);
    }

    @Transactional
    public void addFacilityToEquipment(UUID typeId, UUID facilityId) {
        Equipment equipment = find(typeId);
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException(facilityId));

        // Проверяем, если связь уже существует
        boolean exists = equipment.getFacilities().stream()
                .anyMatch(tf -> tf.getFacility().getId().equals(facilityId));
        if (!exists) {
            // Создаём новую связь и добавляем её в список связей типа
            EquipmentFacility equipmentFacility = new EquipmentFacility(new EquipmentFacilityKey(typeId, facilityId), equipment, facility);
            equipment.getFacilities().add(equipmentFacility);
            equipmentRepository.save(equipment); // Сохраняем изменения
        }
    }

    @Transactional
    public void removeFacilityFromEquipment(UUID equipmentId, UUID facilityId) {
        // Создаем идентификатор для связи
        EquipmentFacilityKey key = new EquipmentFacilityKey(equipmentId, facilityId);

        // Находим объект EquipmentFacility по ключу
        EquipmentFacility equipmentFacility = equipmentFacilityRepository.findById(key)
                .orElseThrow(() -> new EFacilityNotFoundException(key));

        // Удаляем связь из Equipment
        Equipment equipment = equipmentFacility.getEquipment();
        equipment.getFacilities().remove(equipmentFacility);

        // Удаляем связь из Facility
        Facility facility = equipmentFacility.getFacility();
        facility.getEquipments().remove(equipmentFacility);

        // Удаляем запись из базы данных
        equipmentFacilityRepository.delete(equipmentFacility);
    }
    @Transactional
    public Equipment delete(UUID id) {

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new EquipmentNotFoundException(id));
        // Удаляем связи между Equipment и Facility
        equipmentRepository.deleteEquipmentFacilities(id);

        // Удаляем саму сущность Equipment
        equipmentRepository.deleteEquipment(id);

        return equipment;
    }

    @Transactional
    public boolean isExistType(UUID typeId)
    {
        return equipmentRepository.existsByTypeId(typeId);
    }
    @Transactional
    public Equipment updateStatus(UUID id,String bool)
    {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new EquipmentNotFoundException(id));
        if(equipment.getStatus() == Status.CREATE){
            equipment.setStatus(Status.SEND);
        }
        else if(equipment.getStatus() == Status.SEND && bool.equals("true"))
        {
            equipment.setStatus(Status.IN_WORK);
        }
        else if(equipment.getStatus() == Status.SEND && bool.equals("false"))
        {
            equipmentRepository.deleteEquipmentFacilities(id);
            equipment.setStatus(Status.CREATE);
        }
        else if(equipment.getStatus() == Status.IN_WORK)
        {
            equipment.setStatus(Status.READY);
        }
        List<UUID> adminsId = userService.getAdmins();

        for (UUID adminId : adminsId){
            log.info(adminId.toString());
            userService.sendNotificationAboutEquipments("Был изменен статус " + equipment.getName() + " статус: "+equipment.getStatus(),adminId);
        }
        userService.sendNotificationAboutEquipments("Был изменен статус " + equipment.getName() + " статус: "+equipment.getStatus(),equipment.getUser().getId());
        return equipmentRepository.save(equipment);
    }


}
