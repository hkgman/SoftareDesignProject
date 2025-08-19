package com.utility.company;

import com.utility.company.dto.TypeDto;
import com.utility.company.error.exception.TypeNotFoundException;
import com.utility.company.model.Type;
import com.utility.company.service.TypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;


import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TypeServiceH2Test {

    @Autowired
    private TypeService typeService;

    @Test
    void testCreateType() {
        TypeDto typeDto = new TypeDto();
        typeDto.setText("Test Type");

        Type createdType = typeService.createType(typeDto);

        assertNotNull(createdType.getId());
        assertEquals("Test Type", createdType.getText());
    }

    @Test
    void testFindByName() {
        TypeDto typeDto = new TypeDto();
        typeDto.setText("Unique Type");
        typeService.createType(typeDto);

        Type foundType = typeService.findByName("Unique Type");

        assertNotNull(foundType);
        assertEquals("Unique Type", foundType.getText());
    }

    @Test
    void testUpdateType() {
        TypeDto typeDto = new TypeDto();
        typeDto.setText("Old Type");
        Type createdType = typeService.createType(typeDto);

        TypeDto updatedTypeDto = new TypeDto();
        updatedTypeDto.setText("Updated Type");
        Type updatedType = typeService.updateType(createdType.getId(), updatedTypeDto);

        assertNotNull(updatedType);
        assertEquals("Updated Type", updatedType.getText());
    }

    @Test
    void testDeleteType() {
        TypeDto typeDto = new TypeDto();
        typeDto.setText("Type To Delete");
        Type createdType = typeService.createType(typeDto);

        String result = typeService.deleteType(createdType.getId());
        assertEquals("Тип техники успешно удален.", result);

        assertThrows(TypeNotFoundException.class, () -> typeService.findType(createdType.getId()));
    }

    @Test
    void testDeleteTypeNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        assertThrows(TypeNotFoundException.class, () -> typeService.deleteType(nonExistentId));
    }

    @Test
    void testFindTypeNotFound() {
        UUID randomId = UUID.randomUUID();
        assertThrows(TypeNotFoundException.class, () -> typeService.findType(randomId));
    }

    @Test
    void testFindType() {

        TypeDto typeDto = new TypeDto();
        typeDto.setText("Findable Type");
        Type createdType = typeService.createType(typeDto);

        Type foundType = typeService.findType(createdType.getId());

        assertNotNull(foundType);
        assertEquals(createdType.getId(), foundType.getId());
        assertEquals("Findable Type", foundType.getText());
    }
}

