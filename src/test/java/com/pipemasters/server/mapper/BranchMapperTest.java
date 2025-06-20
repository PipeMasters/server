package com.pipemasters.server.mapper;

import com.pipemasters.server.TestEnvInitializer;
import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.entity.Branch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(initializers = TestEnvInitializer.class)
public class BranchMapperTest {

    @Autowired
    private ModelMapper modelMapper;

    @Test
    void shouldMapBranchToBranchDtoCorrectly() {
        Branch parent = new Branch("Head Office", null);
        Branch child = new Branch("Regional Office", parent);

        BranchDto dto = modelMapper.map(child, BranchDto.class);

        assertEquals("Regional Office", dto.getName());
        assertNull(dto.getParent(), "Поле parent должно быть null из-за .skip()");
    }

    @Test
    void shouldMapBranchDtoToBranchCorrectly() {
        BranchDto parentDto = new BranchDto("Head Office", null);
        BranchDto childDto = new BranchDto("Regional Office", parentDto);

        Branch entity = modelMapper.map(childDto, Branch.class);

        assertEquals("Regional Office", entity.getName());
        assertNotNull(entity.getParent(), "Поле parent должно быть замаплено в обратную сторону");
        assertEquals("Head Office", entity.getParent().getName());
    }

    @Test
    void shouldNotCauseRecursionWhenMappingEntityWithDeepHierarchy() {
        Branch root = new Branch("Root", null);
        Branch level1 = new Branch("Level1", root);
        Branch level2 = new Branch("Level2", level1);
        Branch level3 = new Branch("Level3", level2);

        BranchDto dto = modelMapper.map(level3, BranchDto.class);

        assertEquals("Level3", dto.getName());
        assertNull(dto.getParent(), "Глубокая иерархия должна быть прервана на первом уровне");
    }
}
