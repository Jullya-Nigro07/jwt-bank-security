package dio.web.JWTBankSecurity.service;

import dio.web.JWTBankSecurity.dto.request.AccountRequest;
import dio.web.JWTBankSecurity.dto.response.AccountResponse;
import dio.web.JWTBankSecurity.entity.Account;
import dio.web.JWTBankSecurity.entity.Transaction;
import dio.web.JWTBankSecurity.entity.User;
import dio.web.JWTBankSecurity.exception.ValueInvalidException;
import dio.web.JWTBankSecurity.repository.AccountRepository;
import dio.web.JWTBankSecurity.repository.TransactionRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Set;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private AccountService accountService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    private Validator validator;

    @BeforeEach
    void setUp() {
        // Inicializa o validador correto do Jakarta/Hibernate
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void shouldAcceptDeposit(){
        User user = new User();
        Account account = new Account();
        user.setAccount(account);
        when(authorizationService.getAuthenticatedUser()).thenReturn(user);

        AccountRequest accountRequest = new AccountRequest(new BigDecimal("100"));
        ResponseEntity<AccountResponse> response = accountService.deposit(accountRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountRepository).save(account);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0.0"})
    void shouldRejectInvalidAmount(String amount) {
        AccountRequest dto = new AccountRequest(new BigDecimal(amount));
        Set<ConstraintViolation<AccountRequest>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }

    @Test
    void shouldAcceptValidAmount() {
        AccountRequest dto = new AccountRequest(new BigDecimal("0.1"));
        Set<ConstraintViolation<AccountRequest>> violations = validator.validate(dto);
        assertEquals(0, violations.size());
    }

    @Test
    void shouldAcceptWithdraw(){
        User user = new User();
        Account account = new Account();
        user.setAccount(account);
        account.setBalance(new BigDecimal("100"));
        when(authorizationService.getAuthenticatedUser()).thenReturn(user);

        AccountRequest accountRequest = new AccountRequest(new BigDecimal("100"));
        ResponseEntity<AccountResponse> response = accountService.withdraw(accountRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountRepository).save(account);
    }

    @Test
    void shouldRejectWithdraw(){
        User user = new User();
        Account account = new Account();
        user.setAccount(account);
        when(authorizationService.getAuthenticatedUser()).thenReturn(user);
        AccountRequest accountRequest = new AccountRequest(new BigDecimal("0.1"));

        assertThrows(ValueInvalidException.class, () -> {
            accountService.withdraw(accountRequest);
        });
    }
}
