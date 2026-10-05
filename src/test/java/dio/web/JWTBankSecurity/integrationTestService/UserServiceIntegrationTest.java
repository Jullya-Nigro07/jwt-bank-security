package dio.web.JWTBankSecurity.integrationTestService;

import dio.web.JWTBankSecurity.dto.request.LoginRequest;
import dio.web.JWTBankSecurity.dto.request.RegisterUserRequest;
import dio.web.JWTBankSecurity.dto.request.UpdateUserRequest;
import dio.web.JWTBankSecurity.dto.response.LoginResponse;
import dio.web.JWTBankSecurity.dto.response.UserResponse;
import dio.web.JWTBankSecurity.entity.User;
import dio.web.JWTBankSecurity.exception.ConflitInfoException;
import dio.web.JWTBankSecurity.repository.UserRepository;
import dio.web.JWTBankSecurity.service.AuthorizationService;
import dio.web.JWTBankSecurity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @MockitoBean
    private AuthorizationService authorizationService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldSaveUser() {
        RegisterUserRequest userRequest = new RegisterUserRequest("Teste", "test@teste.com", "1234567");
        ResponseEntity<UserResponse> response = userService.register(userRequest);
        User user = userRepository.findUserByEmail(response.getBody().email()).orElseThrow();

        assertAll(
                () -> assertNotNull(user),
                () -> assertEquals(HttpStatus.CREATED, response.getStatusCode())
        );
    }

    @Test
    void shouldHasePassword(){
        String password = "1234567";
        RegisterUserRequest userRequest = new RegisterUserRequest("Teste", "test@teste.com", password);
        userService.register(userRequest);

        User user = userRepository.findUserByEmail(userRequest.email()).orElseThrow();

        assertAll(
                () -> assertNotEquals(password, user.getPassword()),
                () -> assertTrue(passwordEncoder.matches(password, user.getPassword()))
        );
    }

    @Test
    void shouldRejectDuplicateEmail() {
        RegisterUserRequest user = new RegisterUserRequest("Teste", "test@teste.com", "1234567");
        RegisterUserRequest user2 = new RegisterUserRequest("Teste", "test@teste.com", "1234567");
        userService.register(user);

        assertThrows(ConflitInfoException.class, () -> {
            userService.register(user2);
        });
    }

    @Test
    void shouldGenerateTokenUser(){
        RegisterUserRequest user = new RegisterUserRequest("Teste", "test@teste.com", "1234567");
        userService.register(user);

        LoginRequest loginUser = new LoginRequest(user.email(), user.password());
        ResponseEntity<LoginResponse> responseLogin = userService.login(loginUser);

        assertAll(
                () -> assertNotNull(responseLogin.getBody()),
                () -> assertEquals(HttpStatus.OK, responseLogin.getStatusCode())
        );
    }

    @Test
    void shouldUpdateUser(){
        RegisterUserRequest userRequest = new RegisterUserRequest("Teste01", "teste01@teste.com", "1234567");
        userService.register(userRequest);

        User user = userRepository.findUserByEmail(userRequest.email()).orElseThrow();
        when(authorizationService.getAuthenticatedUser()).thenReturn(user);

        UpdateUserRequest userRequest2 = new UpdateUserRequest("Teste02", "teste02@teste.com", "7654321");
        ResponseEntity<UserResponse> response = userService.updateUser(userRequest2);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("Teste02", user.getName()),
                () -> assertEquals("teste02@teste.com", user.getEmail()),
                () -> assertTrue(passwordEncoder.matches("7654321", user.getPassword()))
        );
    }

    @Test
    void shouldDeleteUser(){
        RegisterUserRequest userRequest = new RegisterUserRequest("Teste", "test@teste.com", "1234567");
        userService.register(userRequest);

        User userRepo = userRepository.findUserByEmail(userRequest.email()).orElseThrow();
        when(authorizationService.getAuthenticatedUser()).thenReturn(userRepo);

        ResponseEntity<UserResponse> user = userService.deleteUser();

        assertAll(
                () -> assertTrue(userRepository.findUserByEmail(user.getBody().email()).isEmpty()),
                () -> assertEquals(HttpStatus.OK, user.getStatusCode())
        );
    }
}