
package com.example.bank_transfer_app.controller;

import com.example.bank_transfer_app.dto.TransferRequest;
import com.example.bank_transfer_app.entity.BankTransaction;
import com.example.bank_transfer_app.entity.Customer;
import com.example.bank_transfer_app.service.BankService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BankController {

    private final BankService bankService;

    // Constructor injection
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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaction);
    }

    // 4. Get all transactions
    @GetMapping("/transactions")
    public ResponseEntity<List<BankTransaction>> getAllTransactions() {
        return ResponseEntity.ok(bankService.getAllTransactions());
    }

    // 5. TASK 12.1: Get large successful transfers
    @GetMapping("/large-transfers")
    public ResponseEntity<List<BankTransaction>> getLargeTransfers(
            @RequestParam(defaultValue = "10000") BigDecimal min) {

        List<BankTransaction> transactions =
                bankService.getLargeTransfers(min);

        return ResponseEntity.ok(transactions);
    }

    // 6. TASK 12.2: Get total successful amount sent by a customer
    @GetMapping("/customers/{customerId}/total-sent")
    public ResponseEntity<BigDecimal> getTotalAmountSent(
            @PathVariable Long customerId) {

        BigDecimal totalAmount =
                bankService.getTotalAmountSent(customerId);

        return ResponseEntity.ok(totalAmount);
    }
}
