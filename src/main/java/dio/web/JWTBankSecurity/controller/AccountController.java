package dio.web.JWTBankSecurity.controller;

import dio.web.JWTBankSecurity.dto.request.AccountRequest;
import dio.web.JWTBankSecurity.dto.response.AccountResponse;
import dio.web.JWTBankSecurity.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;
    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountResponse> deposit(@Valid @RequestBody AccountRequest request){
        return accountService.deposit(request);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<AccountResponse> withdraw(@Valid @RequestBody AccountRequest request){
        return accountService.withdraw(request);
    }

    @GetMapping("/extract")
    private ResponseEntity<AccountResponse> extract(){
        return accountService.extract();
    }
}