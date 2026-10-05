package dio.web.JWTBankSecurity.integrationTestService;

import dio.web.JWTBankSecurity.dto.request.AccountRequest;
import dio.web.JWTBankSecurity.dto.response.AccountResponse;
import dio.web.JWTBankSecurity.entity.Account;
import dio.web.JWTBankSecurity.entity.User;
import dio.web.JWTBankSecurity.repository.AccountRepository;
import dio.web.JWTBankSecurity.repository.UserRepository;
import dio.web.JWTBankSecurity.service.AccountService;
import dio.web.JWTBankSecurity.service.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceIntegrationTest {
    @MockitoBean
    private AuthorizationService authorizationService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldDeposit(){
        User user = new User("Test", "test@gmail.com", "12345678");
        Account account = new Account();
        
        user.setAccount(account);
        account.setUser(user);
        userRepository.save(user);
        accountRepository.save(account);

        when(authorizationService.getAuthenticatedUser()).thenReturn(user);

        AccountRequest request = new AccountRequest(new BigDecimal("100"));
        ResponseEntity<AccountResponse> response = accountService.deposit(request);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(user.getAccount().getBalance(), request.amount())
        );
    }

    @Test
    void shouldWithdraw(){
        User user = new User("Test", "test@gmail.com", "12345678");
        Account account = new Account();

        user.setAccount(account);
        account.setUser(user);
        account.setBalance(new BigDecimal("250"));
        userRepository.save(user);
        accountRepository.save(account);

        when(authorizationService.getAuthenticatedUser()).thenReturn(user);

        AccountRequest request = new AccountRequest(new BigDecimal("100"));
        ResponseEntity<AccountResponse> response = accountService.withdraw(request);

        BigDecimal value = new BigDecimal("150");

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(user.getAccount().getBalance(), value)
        );
    }
    
    @Test
    void shouldExtract(){
        User user = new User("Test", "test@gmail.com", "12345678");
        Account account = new Account();

        user.setAccount(account);
        account.setUser(user);
        userRepository.save(user);
        accountRepository.save(account);

        when(authorizationService.getAuthenticatedUser()).thenReturn(user);
        ResponseEntity<AccountResponse> response = accountService.extract();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
