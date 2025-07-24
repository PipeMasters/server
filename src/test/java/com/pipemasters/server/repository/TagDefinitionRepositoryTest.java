package com.pipemasters.server.repository;

import com.pipemasters.server.entity.TagDefinition;
import com.pipemasters.server.entity.enums.TagType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TagDefinitionRepositoryTest {

    @Autowired
    private TagDefinitionRepository tagDefinitionRepository;


    @Test
    void findAllReturnsEmptyListWhenNoTagDefinitionsExist() {
        List<TagDefinition> all = tagDefinitionRepository.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void findByIdReturnsEmptyOptionalForNonExistentTagDefinition() {
        assertTrue(tagDefinitionRepository.findById(999L).isEmpty());
    }

    @Test
    void existsByIdReturnsFalseForNonExistentTagDefinition() {
        assertFalse(tagDefinitionRepository.existsById(12345L));
    }

    @Test
    void saveTagDefinitionPersistsSuccessfully() {
        TagDefinition definition = new TagDefinition("Тест_Название", TagType.RULE);
        TagDefinition savedDefinition = tagDefinitionRepository.save(definition);

        assertNotNull(savedDefinition.getId());
        assertEquals("Тест_Название", savedDefinition.getName());
        assertEquals(TagType.RULE, savedDefinition.getType());

        Optional<TagDefinition> found = tagDefinitionRepository.findById(savedDefinition.getId());
        assertTrue(found.isPresent());
        assertEquals("Тест_Название", found.get().getName());
        assertEquals(TagType.RULE, found.get().getType());
    }

    @Test
    void findByNameAndTypeReturnsTagDefinitionWhenExists() {
        TagDefinition definition1 = new TagDefinition("Пример_Тега1", TagType.RULE);
        tagDefinitionRepository.save(definition1);

        Optional<TagDefinition> found = tagDefinitionRepository.findByNameAndType("Пример_Тега1", TagType.RULE);
        assertTrue(found.isPresent());
        assertEquals("Пример_Тега1", found.get().getName());
        assertEquals(TagType.RULE, found.get().getType());
    }

    @Test
    void findByNameAndTypeReturnsEmptyOptionalWhenNotExists() {
        Optional<TagDefinition> found = tagDefinitionRepository.findByNameAndType("Несуществующий_Тег", TagType.RULE);
        assertTrue(found.isEmpty());
    }
}