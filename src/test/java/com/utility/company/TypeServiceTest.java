package com.utility.company;

import com.utility.company.dto.TypeDto;
import com.utility.company.error.exception.TypeNotFoundException;
import com.utility.company.model.Facility;
import com.utility.company.model.Type;
import com.utility.company.repository.TypeRepository;
import com.utility.company.service.EquipmentService;
import com.utility.company.service.TypeService;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TypeServiceTest {

	@Mock
	private TypeRepository typeRepository;

	@Mock
	private EquipmentService equipmentService;

	@InjectMocks
	private TypeService typeService;

	private TypeDto typeDto;
	private Type existingType;
	private UUID typeId;

	@BeforeEach
	void setUp() {
		typeId = UUID.randomUUID();
		existingType = new Type(typeId, "Existing Type");
		typeDto = new TypeDto();
		typeDto.setText("New Type");
	}

	@Test
	void shouldCreateType_whenTypeDoesNotExist() {
		when(typeRepository.findOneByTextIgnoreCase(typeDto.getText())).thenReturn(null);
		when(typeRepository.save(any(Type.class))).thenReturn(new Type(UUID.randomUUID(), typeDto.getText()));

		Type createdType = typeService.createType(typeDto);

		assertNotNull(createdType);
		assertEquals(typeDto.getText(), createdType.getText());
		verify(typeRepository, times(1)).save(any(Type.class));
	}

	@Test
	void shouldThrowValidationException_whenTypeAlreadyExists() {
		when(typeRepository.findOneByTextIgnoreCase(typeDto.getText())).thenReturn(existingType);

		ValidationException thrown = assertThrows(ValidationException.class, () -> typeService.createType(typeDto));
		assertEquals("Type 'Existing Type' already exists", thrown.getMessage());
	}

	@Test
	void shouldUpdateType_whenTypeExists() {
		when(typeRepository.findById(typeId)).thenReturn(Optional.of(existingType));

		Type updatedType = new Type(typeId, "Updated Type");
		when(typeRepository.save(any(Type.class))).thenReturn(updatedType);
		TypeDto updatedDto = new TypeDto();
		updatedDto.setText("Updated Type");

		Type result = typeService.updateType(typeId, updatedDto);

		assertNotNull(result);
		assertEquals("Updated Type", result.getText());
		verify(typeRepository, times(1)).save(any(Type.class));
	}

	@Test
	void shouldThrowTypeNotFoundException_whenUpdatingNonExistentType() {
		when(typeRepository.findById(typeId)).thenReturn(Optional.empty());

		TypeNotFoundException thrown = assertThrows(TypeNotFoundException.class, () -> typeService.updateType(typeId, typeDto));
		assertTrue(thrown.getMessage().contains(typeId.toString()));
	}

	@Test
	void shouldDeleteType_whenTypeExistsAndNotUsed() {
		when(equipmentService.isExistType(typeId)).thenReturn(false);
		Type type = new Type(); // Можете задать поля объекта при необходимости
		when(typeRepository.findById(typeId)).thenReturn(Optional.of(type));
		String result = typeService.deleteType(typeId);

		assertEquals("Тип техники успешно удален.", result);
		verify(typeRepository, times(1)).deleteById(typeId);
	}

	@Test
	void shouldNotDeleteType_whenTypeIsUsed() {
		when(equipmentService.isExistType(typeId)).thenReturn(true);
		Type type = new Type(); // Можете задать поля объекта при необходимости
		when(typeRepository.findById(typeId)).thenReturn(Optional.of(type));
		String result = typeService.deleteType(typeId);

		assertEquals("Прямо сейчас существует техника с такиим типом. Невозможно удалить.", result);
		verify(typeRepository, never()).deleteById(typeId);
	}

	@Test
	void shouldFindTypeById_whenExists() {

		when(typeRepository.findById(typeId)).thenReturn(Optional.of(existingType));

		Type foundType = typeService.findType(typeId);

		assertNotNull(foundType);
		assertEquals(typeId, foundType.getId());
	}

	@Test
	void shouldThrowTypeNotFoundException_whenTypeNotFoundById() {
		when(typeRepository.findById(typeId)).thenReturn(Optional.empty());

		TypeNotFoundException thrown = assertThrows(TypeNotFoundException.class, () -> typeService.findType(typeId));
		assertTrue(thrown.getMessage().contains(typeId.toString()));
	}

	@Test
	void shouldReturnPageOfTypes() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Type> page = mock(Page.class);
		when(typeRepository.findAll(pageable)).thenReturn(page);

		Page<Type> result = typeService.findAllPageable(pageable);

		assertNotNull(result);
		verify(typeRepository, times(1)).findAll(pageable);
	}

	@Test
	void shouldReturnListOfAllTypes() {
		when(typeRepository.findAll()).thenReturn(List.of(existingType));

		List<Type> result = typeService.findAll();

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(typeRepository, times(1)).findAll();
	}
}
