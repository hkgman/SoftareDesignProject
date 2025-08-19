package com.utility.company.service;

import com.utility.company.dto.TypeDto;
import com.utility.company.error.exception.TypeNotFoundException;
import com.utility.company.model.Type;
import com.utility.company.repository.TypeRepository;
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
public class TypeService {
    private final TypeRepository typeRepository;
    private final EquipmentService equipmentService;
    @Transactional(readOnly = true)
    public Type findByName(String name) {
        return typeRepository.findOneByTextIgnoreCase(name);
    }
    @Transactional
    public Type createType(TypeDto typeDto) {
        final Type type = findByName(typeDto.getText());
        if (type != null) {
            throw new ValidationException(String.format("Type '%s' already exists", type.getText()));
        }
        final Type newType = new Type(null, typeDto.getText());
        return typeRepository.save(newType);
    }

    @Transactional(readOnly = true)
    public Type findType(UUID id) {
        final Optional<Type> base = typeRepository.findById(id);
        return base.orElseThrow(() -> new TypeNotFoundException(id));
    }



    @Transactional
    public Type updateType(UUID id, TypeDto typeDto) {
        final Type curType = findType(id);
        curType.setText(typeDto.getText());
        return typeRepository.save(curType);
    }

    @Transactional
    public String deleteType(UUID id) {
        Type type = typeRepository.findById(id)
                .orElseThrow(() -> new TypeNotFoundException(id));
        boolean isUsed = equipmentService.isExistType(id);
        if(isUsed)
        {
            return "Прямо сейчас существует техника с такиим типом. Невозможно удалить.";
        }
        typeRepository.deleteById(id);
        return "Тип техники успешно удален.";
    }
    @Transactional(readOnly = true)
    public Page<Type> findAllPageable(Pageable pageable) {
        return typeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Type> findAll() {
        return typeRepository.findAll();
    }
}
