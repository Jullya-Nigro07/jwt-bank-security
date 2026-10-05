package dio.web.JWTBankSecurity.unitTestService;

import dio.web.JWTBankSecurity.dto.response.TransactionResponse;
import dio.web.JWTBankSecurity.entity.Account;
import dio.web.JWTBankSecurity.entity.Transaction;
import dio.web.JWTBankSecurity.entity.User;
import dio.web.JWTBankSecurity.enums.TipoTransaction;
import dio.web.JWTBankSecurity.repository.TransactionRepository;
import dio.web.JWTBankSecurity.service.AuthorizationService;
import dio.web.JWTBankSecurity.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldEmptyTransactions() {
        User user = new User("Ju", "ju@gmail.com", "1234567");
        Account account = new Account();

        user.setAccount(account);
        account.setUser(user);

        when(authorizationService.getAuthenticatedUser()).thenReturn(user);
        when(transactionRepository.findByAccountId(user.getId())).thenReturn(List.of());

        List<TransactionResponse> response = transactionService.findMyTransactions();

        assertTrue(response.isEmpty());
        verify(authorizationService).getAuthenticatedUser();
    }

    @Test
    void shouldTransactions(){
        User user = new User();
        Account account = new Account();
        user.setAccount(account);
        account.setId(1L);

        Transaction transaction1 = new Transaction();
        transaction1.setType(TipoTransaction.DEPOSIT);
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction1.setDateTime(LocalDateTime.now());

        Transaction transaction2 = new Transaction();
        transaction2.setType(TipoTransaction.WITHDRAW);
        transaction2.setAmount(new BigDecimal("50.00"));
        transaction2.setDateTime(LocalDateTime.now());

        when(authorizationService.getAuthenticatedUser()).thenReturn(user);
        when(transactionRepository.findByAccountId(account.getId())).thenReturn(List.of(transaction1, transaction2));
        List<TransactionResponse> response = transactionService.findMyTransactions();

        assertAll(
                () -> assertEquals(2, response.size()),
                () -> assertEquals(TipoTransaction.DEPOSIT, response.get(0).type()),
                () -> assertEquals(new BigDecimal("100.00"), response.get(0).amount()),
                () -> assertEquals(TipoTransaction.WITHDRAW, response.get(1).type()),
                () -> assertEquals(new BigDecimal("50.00"), response.get(1).amount())
        );

        verify(authorizationService).getAuthenticatedUser();
        verify(transactionRepository).findByAccountId(1L);
    }
}
