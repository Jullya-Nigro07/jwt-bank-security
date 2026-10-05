package dio.web.JWTBankSecurity.integrationTestService;

import dio.web.JWTBankSecurity.dto.response.TransactionResponse;
import dio.web.JWTBankSecurity.entity.Account;
import dio.web.JWTBankSecurity.entity.Transaction;
import dio.web.JWTBankSecurity.entity.User;
import dio.web.JWTBankSecurity.enums.TipoTransaction;
import dio.web.JWTBankSecurity.repository.TransactionRepository;
import dio.web.JWTBankSecurity.repository.UserRepository;
import dio.web.JWTBankSecurity.service.AuthorizationService;
import dio.web.JWTBankSecurity.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private AuthorizationService authorizationService;

    @Test
    void shouldFindMyTransactions() {
        User user = new User("Jullya", "jullya@email.com", "1234567");
        Account account = new Account();

        user.setAccount(account);
        account.setUser(user);
        userRepository.save(user);

        Transaction transaction1 = new Transaction();
        transaction1.setAccount(account);
        transaction1.setType(TipoTransaction.DEPOSIT);
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction1.setDateTime(LocalDateTime.now());

        Transaction transaction2 = new Transaction();
        transaction2.setAccount(account);
        transaction2.setType(TipoTransaction.WITHDRAW);
        transaction2.setAmount(new BigDecimal("30.00"));
        transaction2.setDateTime(LocalDateTime.now());

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);

        when(authorizationService.getAuthenticatedUser()).thenReturn(user);
        List<TransactionResponse> response = transactionService.findMyTransactions();

        assertEquals(2, response.size());
        assertEquals(new BigDecimal("100.00"), response.get(0).amount());
        assertEquals(new BigDecimal("30.00"), response.get(1).amount());

        verify(authorizationService).getAuthenticatedUser();
    }
}
