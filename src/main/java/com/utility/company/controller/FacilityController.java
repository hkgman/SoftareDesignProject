package com.utility.company.controller;

import com.utility.company.aspect.CustomSecured;
import com.utility.company.dto.FacilityDto;
import com.utility.company.dto.ViewFacilityDto;
import com.utility.company.model.Facility;
import com.utility.company.model.enums.UserRole;
import com.utility.company.service.FacilityService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/facility")
@Slf4j
public class FacilityController {
    private final FacilityService facilityService;
    @GetMapping(value = {"/edit", "/edit/{id}"})
    @CustomSecured(role= {UserRole.AsString.ADMIN})
    public String editFacility(@PathVariable(required = false) UUID id,
                                  Model model) {
        if (id == null) {
            model.addAttribute("facilityDto", new FacilityDto());
        } else {
            model.addAttribute("facilityId", id);
            model.addAttribute("facilityDto", new FacilityDto(facilityService.findFacility(id)));
        }
        return "facility-edit";
    }

    @PostMapping(value = {"/", "/{id}"})
    @CustomSecured(role= {UserRole.AsString.ADMIN})
    public String saveFacility(@PathVariable(required = false) UUID id,
                                  @ModelAttribute("facilityDto") @Valid FacilityDto facilityDto,
                                  BindingResult bindingResult,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "facility-edit";
        }
        try {
            if ( id == null) {
                facilityService.createFacility(facilityDto);
            } else {
                facilityService.updateFacility(id, facilityDto);
            }
            return "redirect:/facility";
        } catch (ValidationException e) {
            model.addAttribute("errors", e.getMessage());
            return "facility-edit";
        }
    }
    @PostMapping("/delete/{id}")
    @CustomSecured(role = {UserRole.AsString.ADMIN})
    public String deleteFacility(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        String result = facilityService.deleteFacility(id);

        redirectAttributes.addFlashAttribute("message", result);
        return "redirect:/facility";
    }

    @GetMapping
    @CustomSecured(role= {UserRole.AsString.ADMIN})
    public String getFacility(Model model, Pageable pageable){
        Page<Facility> facilities = facilityService.findAllFacilityList(PageRequest.of(pageable.getPageNumber(), 4));
        model.addAttribute("currentPage", pageable.getPageNumber());
        model.addAttribute("totalPages", facilities.getTotalPages());
        model.addAttribute("facilities",
                facilities.stream()
                        .map(ViewFacilityDto::new)
                        .toList());
        if (model.containsAttribute("message")) {
            model.addAttribute("message", model.getAttribute("message"));
        }
        return "facility";
    }
}
