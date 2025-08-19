package com.utility.company.controller;


import com.utility.company.aspect.CustomSecured;
import com.utility.company.dto.*;
import com.utility.company.model.*;
import com.utility.company.model.enums.UserRole;
import com.utility.company.repository.FacilityRepository;
import com.utility.company.service.EquipmentService;
import com.utility.company.service.TypeService;
import com.utility.company.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/equipment")
@Slf4j
public class EquipmentController {
    private final EquipmentService equipmentService;

    private final UserService userService;
    private final FacilityRepository facilityRepository;
    private final TypeService typeService;

    @GetMapping(value = {"/edit", "/edit/{id}"})
    public String editEquipment(@PathVariable(required = false) UUID id,
                           Model model) {
        if (id == null) {
            model.addAttribute("equipmentDto", new EquipmentDto());
        } else {
            model.addAttribute("equipment", id);
            model.addAttribute("equipmentDto", new EquipmentDto(equipmentService.find(id)));
        }
        model.addAttribute("types",
                typeService.findAll().stream()
                        .map(ViewTypeDto::new)
                        .toList());
        return "equipment-edit";
    }

    @GetMapping(value = {"/pay/{id}"})
    public String payEquipment(@PathVariable UUID id,
                                Model model) {
        Equipment equipment = equipmentService.find(id);
        List<EquipmentFacility> equipmentFacilities = equipmentService.getFacilitiesByEquipment(id);
        Integer priceAll = equipmentFacilities.stream().map(EquipmentFacility::getFacility).mapToInt(Facility::getPrice).sum();
        model.addAttribute("equipment", equipment);
        model.addAttribute("equipmentFacilities", equipmentFacilities);
        model.addAttribute("AllPrice",priceAll);
        return "equipment-pay";
    }
    @PostMapping(value = {"/{id}/pay"})
    @CustomSecured(role= {UserRole.AsString.USER})
    public String pay(@PathVariable UUID id)
    {
        equipmentService.pay(id);
        return "redirect:/equipment";
    }
    @PostMapping(value = {"/", "/{id}"})
    public String saveEquipment(@PathVariable(required = false) UUID id,
                           @ModelAttribute("equipmentDto") @Valid EquipmentDto equipmentDto,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "equipment-edit";
        }
        try {
            if ( id == null) {
                equipmentService.addEquipment(equipmentDto);
            } else {
                equipmentService.updateEquipment(id, equipmentDto);
            }
            return "redirect:/equipment";
        } catch (ValidationException e) {
            model.addAttribute("errors", e.getMessage());
            return "equipment-edit";
        }
    }

    @GetMapping("/{equipmentId}")
    public String getType(Model model, @PathVariable UUID equipmentId)
    {
        Equipment equipment = equipmentService.find(equipmentId);
        List<EquipmentFacility> equipmentFacilities = equipmentService.getFacilitiesByEquipment(equipmentId);
        List<Facility> allFacilities = facilityRepository.findAll();
        model.addAttribute("equipment", equipment);
        model.addAttribute("equipmentFacilities", equipmentFacilities);
        model.addAttribute("allFacilities", allFacilities);
        return "equipment-one";
    }

    @GetMapping("/report")
    @CustomSecured(role = {UserRole.AsString.ADMIN})
    public String getReport(Model model) {
        // Получение отчета из сервиса
        List<EquipmentReportDto> report = equipmentService.getEquipmentReport();
        model.addAttribute("reportList", report);

        // Формирование списка цен для каждого отчета
        List<Integer> allPrices = report.stream()
                .map(dto -> dto.getFacilities() != null
                        ? dto.getFacilities().stream()
                        .mapToInt(EquipmentReportDto.FacilityInfo::getPrice)
                        .sum()
                        : 0) // Если facilities == null, то сумма будет 0
                .collect(Collectors.toList());
        model.addAttribute("allPrices", allPrices);

        // Возвращаем шаблон отчета
        return "equipment-report";
    }

    @PostMapping("/{equipmentId}/status-action")
    public String updateStatus(Model model, @PathVariable UUID equipmentId,@RequestParam String bool)
    {
        equipmentService.updateStatus(equipmentId,bool);
        return "redirect:/equipment/" + equipmentId;
    }

    @PostMapping("/{id}/add-facility")
    @CustomSecured(role= {UserRole.AsString.ADMIN})
    public String addFacilityToType(@PathVariable("id") UUID typeId, @RequestParam("facilityId") UUID facilityId) {
        equipmentService.addFacilityToEquipment(typeId, facilityId);
        return "redirect:/equipment/" + typeId; // Перенаправление обратно на страницу типа
    }

    @GetMapping("/report/{id}")
    public String reportEquipment(@PathVariable("id") UUID equipmentId,Model model)
    {
        EquipmentReportDto equipmentReportDto = equipmentService.getEquipmentOneReport(equipmentId);
        model.addAttribute("report",equipmentReportDto);
        Integer priceAll = equipmentReportDto.getFacilities().stream().mapToInt(EquipmentReportDto.FacilityInfo::getPrice).sum();
        model.addAttribute("AllPrice",priceAll);
        return "equipment-one-report";
    }


    @PostMapping("/deleteFacility/{equipmentId}/{facilityId}")
    @CustomSecured(role= {UserRole.AsString.ADMIN})
    public String removeFacilityToType(@PathVariable("equipmentId") UUID equipmentId,
                                       @PathVariable("facilityId") UUID facilityId) {
        equipmentService.removeFacilityFromEquipment(equipmentId, facilityId);
        return "redirect:/equipment/" + equipmentId; // Перенаправление обратно на страницу типа
    }
    @PostMapping("/delete/{id}")
    public String deleteEquipment(@PathVariable UUID id) {
        equipmentService.delete(id);
        return "redirect:/equipment";
    }

    @GetMapping
    public String getEquipments(Model model, Pageable pageable) {
            User user = null;
            SecurityContext securityContext = SecurityContextHolder.getContext();
            Authentication authentication = securityContext.getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof DefaultOAuth2User userDetails) {
                user = userService.findByEmail(userDetails.getAttribute("email"));
            } else if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
                user = userService.findByEmail(userDetails.getUsername());
            }
            if(user.getRole()==UserRole.ADMIN)
            {
                Page<Equipment> equipments = equipmentService.getAllEquipments(PageRequest.of(pageable.getPageNumber(), 6));
                model.addAttribute("currentPage", pageable.getPageNumber());
                model.addAttribute("totalPages", equipments.getTotalPages());
                model.addAttribute("equipments",
                        equipments.stream()
                                .map(ViewEquipmentDto::new)
                                .toList());
            }
            else
            {
                Page<Equipment> equipments = equipmentService.getEquipmentByUser(PageRequest.of(pageable.getPageNumber(), 6));
                model.addAttribute("currentPage", pageable.getPageNumber());
                model.addAttribute("totalPages", equipments.getTotalPages());
                model.addAttribute("equipments",
                        equipments.stream()
                                .map(ViewEquipmentDto::new)
                                .toList());
            }

        return "equipment";
    }



}
