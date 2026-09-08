package dio.web.JWTBankSecurity.service;

import dio.web.JWTBankSecurity.dto.request.AccountRequest;
import dio.web.JWTBankSecurity.dto.response.AccountResponse;
import dio.web.JWTBankSecurity.entity.Account;
import dio.web.JWTBankSecurity.entity.Transaction;
import dio.web.JWTBankSecurity.entity.User;
import dio.web.JWTBankSecurity.enums.TipoTransaction;
import dio.web.JWTBankSecurity.exception.ValueInvalidException;
import dio.web.JWTBankSecurity.repository.AccountRepository;
import dio.web.JWTBankSecurity.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuthorizationService authorizationService;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository, AuthorizationService authorizationService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public ResponseEntity<AccountResponse> deposit(AccountRequest request) {
        User user = authorizationService.getAuthenticatedUser();

        Account accountUser = user.getAccount();
        BigDecimal amount = request.amount();

        accountUser.setBalance(accountUser.getBalance().add(amount));

        Transaction transaction = new Transaction(TipoTransaction.DEPOSIT, amount, accountUser);

        transactionRepository.save(transaction);
        accountRepository.save(accountUser);

        return ResponseEntity.ok(new AccountResponse(accountUser.getId(), accountUser.getBalance()));
    }

    @Transactional
    public ResponseEntity<AccountResponse> withdraw(AccountRequest request){
        User user = authorizationService.getAuthenticatedUser();

        Account account = user.getAccount();
        BigDecimal amount = request.amount();

        //Primeiro menor que o segundo
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ValueInvalidException("Your balance is insufficient. Check your balance");
        }

        Transaction transaction = new Transaction(TipoTransaction.WITHDRAW, amount, account);
        account.setBalance(account.getBalance().subtract(amount));

        accountRepository.save(account);
        transactionRepository.save(transaction);

        return ResponseEntity.ok(new AccountResponse(account.getId(), account.getBalance()));
    }

    public ResponseEntity<AccountResponse> extract(){
        User user = authorizationService.getAuthenticatedUser();
        Account account = user.getAccount();
        return ResponseEntity.ok(new AccountResponse(account.getId(), account.getBalance()));
    }
}