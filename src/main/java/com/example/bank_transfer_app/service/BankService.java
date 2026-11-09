
package com.example.bank_transfer_app.service;

import com.example.bank_transfer_app.entity.BankTransaction;
import com.example.bank_transfer_app.entity.Customer;
import com.example.bank_transfer_app.entity.TransactionStatus;
import com.example.bank_transfer_app.exception.CustomerNotFoundException;
import com.example.bank_transfer_app.exception.InsufficientBalanceException;
import com.example.bank_transfer_app.exception.InvalidTransferException;
import com.example.bank_transfer_app.repository.BankTransactionRepository;
import com.example.bank_transfer_app.repository.CustomerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;
    private final TransactionAuditService auditService;

    // Constructor injection
    public BankService(
            CustomerRepository customerRepository,
            BankTransactionRepository transactionRepository,
            TransactionAuditService auditService) {

        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
    }

    // TASK 10: Restore transaction management
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount,
            boolean simulateSystemFailure) {

        Long senderId = null;
        Long receiverId = null;

        try {

            // Validate transfer amount
            if (amount == null ||
                    amount.compareTo(BigDecimal.ZERO) <= 0) {

                throw new InvalidTransferException(
                        "Transfer amount must be greater than zero");
            }

            // Find sender
            Customer sender = customerRepository
                    .findByAccountNumber(senderAccountNumber)
                    .orElseThrow(() -> new CustomerNotFoundException(
                            "Sender account not found"));

            senderId = sender.getId();

            // Find receiver
            Customer receiver = customerRepository
                    .findByAccountNumber(receiverAccountNumber)
                    .orElseThrow(() -> new CustomerNotFoundException(
                            "Receiver account not found"));

            receiverId = receiver.getId();

            // Prevent same account transfer
            if (sender.getId().equals(receiver.getId())) {

                throw new InvalidTransferException(
                        "Cannot transfer money to the same account");
            }

            // Check sender balance
            if (sender.getBalance().compareTo(amount) < 0) {

                throw new InsufficientBalanceException(
                        "Insufficient balance in sender account");
            }

            // Debit sender account
            int debitResult = customerRepository.debitBalance(
                    senderAccountNumber, amount);

            if (debitResult == 0) {

                throw new InsufficientBalanceException(
                        "Unable to debit sender account");
            }

            // TASK 10: Simulate system failure after debit
            if (simulateSystemFailure) {

                throw new RuntimeException(
                        "Simulated system failure after debit!");
            }

            // Credit receiver account
            int creditResult = customerRepository.creditBalance(
                    receiverAccountNumber, amount);

            if (creditResult == 0) {

                throw new CustomerNotFoundException(
                        "Unable to credit receiver account");
            }

            // Create successful transaction
            BankTransaction transaction = new BankTransaction();

            transaction.setSender(sender);
            transaction.setReceiver(receiver);
            transaction.setAmount(amount);
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setTransactionDate(LocalDateTime.now());

            return transactionRepository.save(transaction);

        } catch (RuntimeException exception) {

            // Save failed transaction separately
            // REQUIRES_NEW is handled by TransactionAuditService
            if (senderId != null && receiverId != null) {

                try {

                    auditService.logFailedTransaction(
                            senderId,
                            receiverId,
                            amount
                    );

                } catch (RuntimeException auditException) {

                    exception.addSuppressed(auditException);
                }
            }

            throw exception;
        }
    }

    // Get all customers
    public List<Customer> getAllCustomers() {

        return customerRepository.findAll();
    }

    // Get customer by account number
    public Customer getCustomerByAccountNumber(
            String accountNumber) {

        return customerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer account not found"));
    }

    // Get all transactions
    public List<BankTransaction> getAllTransactions() {

        return transactionRepository.findAll();
    }
}
