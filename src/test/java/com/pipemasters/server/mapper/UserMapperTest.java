package com.pipemasters.server.mapper;

import com.pipemasters.server.config.ModelMapperConfig;
import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.dto.UserDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.enums.Role;
import com.pipemasters.server.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.modelmapper.ModelMapper;


import org.junit.jupiter.api.Test;


import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserMapperTest {
    private ModelMapper modelMapper;

    @BeforeEach
    public void setup() {
        modelMapper = new ModelMapperConfig().modelMapper();
    }

    @Test
    void testUserToUserDtoMapping() {
        // Given
        Branch parentBranch = new Branch("Head Office", null);
        parentBranch.setId(10L);

        Branch branch = new Branch("1L", parentBranch);
        branch.setId(20L);

        User user = new User("Иван", "Иванов", "Иванович", Set.of(Role.USER, Role.ADMIN), branch);

        UserDto dto = modelMapper.map(user, UserDto.class);

        assertThat(dto.getName()).isEqualTo("Иван");
        assertThat(dto.getSurname()).isEqualTo("Иванов");
        assertThat(dto.getPatronymic()).isEqualTo("Иванович");
        assertThat(dto.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
        assertThat(dto.getBranchId()).isNotNull();
        assertThat(dto.getBranchId()).isEqualTo(branch.getId());
    }


    @Test
    void testUserDtoToUserMapping() {
        // Given
        Long branchId = 55L;

        UserDto dto = new UserDto("Мария", "Петрова", "Алексеевна", Set.of(Role.USER), branchId);

        // When
        User user = modelMapper.map(dto, User.class);

        Branch branchStub = new Branch("Test Branch", null);
        branchStub.setId(branchId);
        user.setBranch(branchStub);

        // Then
        assertThat(user.getName()).isEqualTo("Мария");
        assertThat(user.getSurname()).isEqualTo("Петрова");
        assertThat(user.getPatronymic()).isEqualTo("Алексеевна");
        assertThat(user.getRoles()).containsExactly(Role.USER);
        assertThat(user.getBranch()).isNotNull();
        assertThat(user.getBranch().getId()).isEqualTo(branchId);
    }
}
