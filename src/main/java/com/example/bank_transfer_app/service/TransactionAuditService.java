
package com.example.bank_transfer_app.service;

import com.example.bank_transfer_app.entity.BankTransaction;
import com.example.bank_transfer_app.entity.Customer;
import com.example.bank_transfer_app.entity.TransactionStatus;
import com.example.bank_transfer_app.repository.BankTransactionRepository;
import com.example.bank_transfer_app.repository.CustomerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionAuditService {

    private final BankTransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    // Constructor injection
    public TransactionAuditService(
            BankTransactionRepository transactionRepository,
            CustomerRepository customerRepository) {

        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
    }

    // Save failed transaction using a separate transaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailedTransaction(
            Long senderId,
            Long receiverId,
            BigDecimal amount) {

        // Find sender
        Customer sender = customerRepository.findById(senderId)
                .orElseThrow(() ->
                        new RuntimeException("Sender not found"));

        // Find receiver
        Customer receiver = customerRepository.findById(receiverId)
                .orElseThrow(() ->
                        new RuntimeException("Receiver not found"));

        // Create failed transaction record
        BankTransaction failedTransaction = new BankTransaction();

        failedTransaction.setSender(sender);
        failedTransaction.setReceiver(receiver);
        failedTransaction.setAmount(amount);
        failedTransaction.setStatus(TransactionStatus.FAILED);
        failedTransaction.setTransactionDate(LocalDateTime.now());

        // Save failed transaction
        transactionRepository.save(failedTransaction);
    }
}
