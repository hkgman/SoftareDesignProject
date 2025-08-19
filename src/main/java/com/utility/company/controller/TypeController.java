package com.utility.company.controller;

import com.utility.company.aspect.CustomSecured;
import com.utility.company.dto.TypeDto;
import com.utility.company.dto.ViewTypeDto;
import com.utility.company.model.Type;
import com.utility.company.model.enums.UserRole;
import com.utility.company.repository.FacilityRepository;
import com.utility.company.service.EquipmentService;
import com.utility.company.service.TypeService;
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
@RequestMapping("/type")
@Slf4j
public class TypeController {
    private final TypeService typeService;
    private final EquipmentService equipmentService;
    private final FacilityRepository facilityRepository;

    @GetMapping(value = {"/edit", "/edit/{id}"})
    @CustomSecured(role= {UserRole.AsString.ADMIN})
    public String editType(@PathVariable(required = false) UUID id,
                               Model model) {
        if (id == null) {
            model.addAttribute("typeDto", new TypeDto());
        } else {
            model.addAttribute("typeId", id);
            model.addAttribute("typeDto", new TypeDto(typeService.findType(id)));
        }
        return "type-edit";
    }

    @PostMapping(value = {"/", "/{id}"})
    @CustomSecured(role= {UserRole.AsString.ADMIN})
    public String saveType(@PathVariable(required = false) UUID id,
                               @ModelAttribute("typeDto") @Valid TypeDto typeDto,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "type-edit";
        }
        try {
            if ( id == null) {
                typeService.createType(typeDto);
            } else {
                typeService.updateType(id, typeDto);
            }
            return "redirect:/type";
        } catch (ValidationException e) {
            model.addAttribute("errors", e.getMessage());
            return "type-edit";
        }
    }
    @PostMapping("/delete/{id}")
    @CustomSecured(role= {UserRole.AsString.ADMIN})
    public String deleteType(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        String result = typeService.deleteType(id);
        redirectAttributes.addFlashAttribute("message", result);
        return "redirect:/type";
    }


    @GetMapping
    @CustomSecured(role= {UserRole.AsString.ADMIN})
    public String getTypes(Model model, Pageable pageable){
        Page<Type> types = typeService.findAllPageable(PageRequest.of(pageable.getPageNumber(), 4));
        model.addAttribute("currentPage", pageable.getPageNumber());
        model.addAttribute("totalPages", types.getTotalPages());
        model.addAttribute("types",
                types.stream()
                        .map(ViewTypeDto::new)
                        .toList());
        if (model.containsAttribute("message")) {
            model.addAttribute("message", model.getAttribute("message"));
        }
        return "type";
    }
}
