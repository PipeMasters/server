package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.AuthenticationRequestDto;
import com.pipemasters.server.dto.request.RegisterRequestDto;
import com.pipemasters.server.dto.request.create.UserCreateDto;
import com.pipemasters.server.dto.response.AuthenticationResponseDto;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.UserAccount;
import com.pipemasters.server.repository.UserAccountRepository;
import com.pipemasters.server.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private ModelMapper modelMapper;

    private AuthenticationServiceImpl authenticationServiceImpl;

    @BeforeEach
    void setUp() {
        authenticationServiceImpl = new AuthenticationServiceImpl(
                userService,
                userAccountRepository,
                passwordEncoder,
                jwtService,
                authenticationManager,
                modelMapper
        );
    }

    @Test
    void register_shouldCreateUserAndAccount_andReturnsToken() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("testuser@mail.com");
        request.setPassword("password123");
        request.setName("Test");
        request.setSurname("User");

        UserCreateDto userCreateDto = new UserCreateDto();
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Test");
        savedUser.setSurname("User");

        String encodedPassword = "encodedPassword123";
        String expectedToken = "jwt.token.string";

        when(modelMapper.map(request, UserCreateDto.class)).thenReturn(userCreateDto);
        when(userService.createAndReturnUser(userCreateDto)).thenReturn(savedUser);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(jwtService.generateToken(any(UserAccount.class))).thenReturn(expectedToken);

        AuthenticationResponseDto response = authenticationServiceImpl.register(request);

        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());

        ArgumentCaptor<UserAccount> userAccountCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(userAccountCaptor.capture());
        UserAccount capturedAccount = userAccountCaptor.getValue();

        assertEquals(request.getUsername(), capturedAccount.getUsername());
        assertEquals(encodedPassword, capturedAccount.getPassword());
        assertSame(savedUser, capturedAccount.getUser());
    }

    @Test
    void login_shouldAuthenticateAndReturnToken() {
        AuthenticationRequestDto request = new AuthenticationRequestDto("testuser", "password");
        UserAccount userAccount = new UserAccount();
        userAccount.setUsername("testuser");
        String expectedToken = "jwt.token.string";

        when(userAccountRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(userAccount));
        when(jwtService.generateToken(userAccount)).thenReturn(expectedToken);

        AuthenticationResponseDto response = authenticationServiceImpl.login(request);

        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userAccountRepository).findByUsername("testuser");
        verify(jwtService).generateToken(userAccount);
    }

    @Test
    void login_shouldThrowException_whenCredentialsAreInvalid() {
        AuthenticationRequestDto request = new AuthenticationRequestDto("testuser", "wrongpassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authenticationServiceImpl.login(request));

        verify(jwtService, never()).generateToken(any());
    }
}