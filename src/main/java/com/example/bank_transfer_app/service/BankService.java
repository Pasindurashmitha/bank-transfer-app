
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;
    private final TransactionAuditService auditService;
    private final TransactionTemplate transactionTemplate;

    // Constructor injection
    public BankService(
            CustomerRepository customerRepository,
            BankTransactionRepository transactionRepository,
            TransactionAuditService auditService,
            PlatformTransactionManager transactionManager) {

        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;

        this.transactionTemplate =
                new TransactionTemplate(transactionManager);

        this.transactionTemplate.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRED);
    }

    // TASK 10: Transfer money with rollback and failed transaction audit
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public BankTransaction transfer(
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount,
            boolean simulateSystemFailure) {

        // Store customer IDs for failed transaction audit
        Long[] customerIds = new Long[2];

        try {

            // Execute transfer inside a separate transaction
            return transactionTemplate.execute(status -> {

                if (amount == null ||
                        amount.compareTo(BigDecimal.ZERO) <= 0) {

                    throw new InvalidTransferException(
                            "Transfer amount must be greater than zero");
                }

                Customer sender = customerRepository
                        .findByAccountNumber(senderAccountNumber)
                        .orElseThrow(() -> new CustomerNotFoundException(
                                "Sender account not found"));

                customerIds[0] = sender.getId();

                Customer receiver = customerRepository
                        .findByAccountNumber(receiverAccountNumber)
                        .orElseThrow(() -> new CustomerNotFoundException(
                                "Receiver account not found"));

                customerIds[1] = receiver.getId();

                if (sender.getId().equals(receiver.getId())) {
                    throw new InvalidTransferException(
                            "Cannot transfer money to the same account");
                }

                if (sender.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientBalanceException(
                            "Insufficient balance in sender account");
                }

                int debitResult = customerRepository.debitBalance(
                        senderAccountNumber, amount);

                if (debitResult == 0) {
                    throw new InsufficientBalanceException(
                            "Unable to debit sender account");
                }

                // Simulate failure after debit for Task 10
                if (simulateSystemFailure) {
                    throw new RuntimeException(
                            "Simulated system failure after debit!");
                }

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
            });

        } catch (RuntimeException exception) {

            // Main transfer transaction has already rolled back here.
            // Save failed transaction in a separate transaction.

            if (customerIds[0] != null &&
                    customerIds[1] != null) {

                try {
                    auditService.logFailedTransaction(
                            customerIds[0],
                            customerIds[1],
                            amount);

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
    public Customer getCustomerByAccountNumber(String accountNumber) {
        return customerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer account not found"));
    }

    // Get all transactions
    public List<BankTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    // TASK 12.1: Get large successful transfers
    public List<BankTransaction> getLargeTransfers(BigDecimal minAmount) {

        if (minAmount == null ||
                minAmount.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Minimum amount cannot be negative or null");
        }

        return transactionRepository.findLargeTransfers(minAmount);
    }

    // TASK 12.2: Get total successful amount sent by a customer
    public BigDecimal getTotalAmountSent(Long customerId) {

        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found"));

        return transactionRepository.getTotalAmountSent(customerId);
    }

    // TASK 12.3: Get or create the system bank account
    private Customer getSystemBankAccount() {

        return customerRepository.findByAccountNumber("999999")
                .orElseGet(() -> {

                    Customer bank = new Customer();

                    bank.setAccountNumber("999999");
                    bank.setName("System Bank");
                    bank.setEmail("bank@bank.com");
                    bank.setBalance(BigDecimal.ZERO);

                    return customerRepository.save(bank);
                });
    }

    // TASK 12.3: Deposit money
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction deposit(
            String accountNumber,
            BigDecimal amount) {

        validateTransactionAmount(amount);

        Customer customer = customerRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer account not found"));

        Customer bank = getSystemBankAccount();

        int result = customerRepository.creditBalance(
                accountNumber, amount);

        if (result == 0) {
            throw new RuntimeException("Deposit failed");
        }

        BankTransaction transaction = new BankTransaction();

        transaction.setSender(bank);
        transaction.setReceiver(customer);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setTransactionDate(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    // TASK 12.3: Withdraw money
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction withdraw(
            String accountNumber,
            BigDecimal amount) {

        validateTransactionAmount(amount);

        Customer customer = customerRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer account not found"));

        Customer bank = getSystemBankAccount();

        int result = customerRepository.debitBalance(
                accountNumber, amount);

        if (result == 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance for withdrawal");
        }

        BankTransaction transaction = new BankTransaction();

        transaction.setSender(customer);
        transaction.setReceiver(bank);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setTransactionDate(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    // Validate deposit and withdrawal amounts
    private void validateTransactionAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidTransferException(
                    "Amount must be greater than zero");
        }
    }
}
