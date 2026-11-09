
package com.example.bank_transfer_app.controller;

import com.example.bank_transfer_app.dto.TransferRequest;
import com.example.bank_transfer_app.entity.BankTransaction;
import com.example.bank_transfer_app.entity.Customer;
import com.example.bank_transfer_app.service.BankService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    // 1. Get all customers
    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {

        return ResponseEntity.ok(bankService.getAllCustomers());
    }

    // 2. Get customer by account number
    @GetMapping("/customers/{accountNumber}")
    public ResponseEntity<Customer> getCustomer(
            @PathVariable String accountNumber) {

        return ResponseEntity.ok(
                bankService.getCustomerByAccountNumber(accountNumber));
    }

    // 3. Transfer money between accounts
    @PostMapping("/transfers")
    public ResponseEntity<BankTransaction> transferMoney(
            @Valid @RequestBody TransferRequest request) {

        BankTransaction transaction = bankService.transfer(
                request.getSenderAccountNumber(),
                request.getReceiverAccountNumber(),
                request.getAmount(),
                request.isSimulateSystemFailure()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    // 4. Get all transactions
    @GetMapping("/transactions")
    public ResponseEntity<List<BankTransaction>> getAllTransactions() {

        return ResponseEntity.ok(bankService.getAllTransactions());
    }
}
