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

    private BranchRepository branchRepository;

    private ModelMapper modelMapper;

    @BeforeEach
    public void setup() {
        modelMapper = new ModelMapperConfig().modelMapper();
    }

    @Test
    void testUserToUserDtoMapping() {
        // Given
        Branch parentBranch = new Branch("Head Office", null);
        Branch branch = new Branch("1L", parentBranch);

        User user = new User("Иван", "Иванов", "Иванович", Set.of(Role.USER, Role.ADMIN), branch);

        UserDto dto = modelMapper.map(user, UserDto.class);

        assertThat(dto.getName()).isEqualTo("Иван");
        assertThat(dto.getSurname()).isEqualTo("Иванов");
        assertThat(dto.getPatronymic()).isEqualTo("Иванович");
        assertThat(dto.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
        assertThat(dto.getBranchId()).isNotNull();
        assertThat(branchRepository.findById(dto.getBranchId()))
                .isPresent()
                .hasValueSatisfying(br ->
                        assertThat(br.getName()).isEqualTo("1L"));
        assertThat(branchRepository.findById(dto.getBranchId()))
                .isPresent()
                .hasValueSatisfying(br ->
                        assertThat(br.getParent()).isNotNull());
    }


    @Test
    void testUserDtoToUserMapping() {
        // Given
        BranchDto branchDtoA = new BranchDto("Branch A", null);
        BranchDto branchDtoB = new BranchDto("Branch B", branchDtoA.getId());
        branchDtoA.setParentId(branchDtoB.getId());
        UserDto dto = new UserDto("Мария", "Петрова", "Алексеевна", Set.of(Role.USER), branchDtoA.getId());

        // When
        User user = modelMapper.map(dto, User.class);

        // Then
        assertThat(user.getName()).isEqualTo("Мария");
        assertThat(user.getSurname()).isEqualTo("Петрова");
        assertThat(user.getPatronymic()).isEqualTo("Алексеевна");
        assertThat(user.getRoles()).containsExactly(Role.USER);
        assertThat(user.getBranch()).isNotNull();
        assertThat(user.getBranch().getName()).isEqualTo("Branch A");
    }
}
