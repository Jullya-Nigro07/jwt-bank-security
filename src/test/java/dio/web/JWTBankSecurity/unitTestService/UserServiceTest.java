package dio.web.JWTBankSecurity.unitTestService;

import dio.web.JWTBankSecurity.config.TokenConfig;
import dio.web.JWTBankSecurity.dto.request.LoginRequest;
import dio.web.JWTBankSecurity.dto.request.RegisterUserRequest;
import dio.web.JWTBankSecurity.dto.request.UpdateUserRequest;
import dio.web.JWTBankSecurity.dto.response.LoginResponse;
import dio.web.JWTBankSecurity.dto.response.UserResponse;
import dio.web.JWTBankSecurity.entity.Account;
import dio.web.JWTBankSecurity.entity.User;
import dio.web.JWTBankSecurity.exception.UnauthorizedException;
import dio.web.JWTBankSecurity.repository.AccountRepository;
import dio.web.JWTBankSecurity.repository.UserRepository;
import dio.web.JWTBankSecurity.service.AuthorizationService;
import dio.web.JWTBankSecurity.service.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.Set;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private Validator validator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenConfig tokenConfig;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuthorizationService authorizationService;


    @BeforeEach
    void setUp() {
        // Inicializa o validador correto do Jakarta/Hibernate
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void shouldInvalidDto(){
        RegisterUserRequest userDto = new RegisterUserRequest(" ", "testdtoteste.com", "123456");
        Set<ConstraintViolation<RegisterUserRequest>> violacoes = validator.validate(userDto);
        assertEquals(3, violacoes.size(), "Existem violações de validação.");
    }

    @Test
    void shouldValidDto(){
        RegisterUserRequest userDto = new RegisterUserRequest("Teste", "testdto@teste.com", "123456789");
        Set<ConstraintViolation<RegisterUserRequest>> violacoes = validator.validate(userDto);
        assertEquals(0, violacoes.size(), "Não existem violações de validação.");
    }

    @Test
    void shouldRegisterUser() {
        RegisterUserRequest request = new RegisterUserRequest("Jullya", "jullya@email.com", "1234567");

        when(userRepository.findUserByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("senha-hash");

        ResponseEntity<UserResponse> response = userService.register(request);

        assertAll(
                () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
                () -> assertNotNull(response.getBody())
        );

        verify(userRepository).findUserByEmail(request.email());
        verify(accountRepository).save(any(Account.class));
        verify(passwordEncoder).encode(request.password());
    }

    @Test
    void shouldLoginUser(){
        User user = new User("test", "test@email.com", "1234567");
        LoginRequest request = new LoginRequest(user.getEmail(), user.getPassword());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(new UsernamePasswordAuthenticationToken(user, null));
        when(tokenConfig.generateToken(user)).thenReturn("token-gerado");

        ResponseEntity<LoginResponse> response = userService.login(request);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("token-gerado", response.getBody().token())
        );

        verify(userRepository).save(user);
        verify(tokenConfig).generateToken(user);
    }

    @Test
    void shouldNotLoginInvalidPassword(){
        User user = new User("test", "test@email.com", "1234567");
        LoginRequest request = new LoginRequest(user.getEmail(), "teste");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(UnauthorizedException.class, () -> {
            userService.login(request);
        });
    }

    @Test
    void shouldNotLoginInvalidEmail(){
        User user = new User("test", "test@email.com", "1234567");
        LoginRequest request = new LoginRequest("teste", user.getPassword());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(UnauthorizedException.class, () -> {
            userService.login(request);
        });
    }

    @Test
    void shouldUpdate(){
        UpdateUserRequest userRequest = new UpdateUserRequest("Test", "teste@gmail.com", "1234567");
        User user = new User();
        when(authorizationService.getAuthenticatedUser()).thenReturn(user);
        when(passwordEncoder.encode(userRequest.password())).thenReturn("senha-hash");
        ResponseEntity<UserResponse> response = userService.updateUser(userRequest);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals("Test", user.getName()),
                () -> assertEquals("teste@gmail.com", user.getEmail()),
                () -> assertEquals("senha-hash", user.getPassword())
        );

        verify(authorizationService).getAuthenticatedUser();
        verify(passwordEncoder).encode(userRequest.password());
        verify(userRepository).save(user);
    }

    @Test
    void shouldDelete(){
        User user = new User();
        when(authorizationService.getAuthenticatedUser()).thenReturn(user);
        ResponseEntity<UserResponse> response = userService.deleteUser();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).delete(user);
    }
}
