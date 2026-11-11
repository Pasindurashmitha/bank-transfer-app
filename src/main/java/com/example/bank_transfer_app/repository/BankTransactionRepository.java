
package com.example.bank_transfer_app.repository;

import com.example.bank_transfer_app.entity.BankTransaction;
import com.example.bank_transfer_app.entity.TransactionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BankTransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    // 1. Get latest transactions
    @Query("SELECT t FROM BankTransaction t ORDER BY t.transactionDate DESC")
    List<BankTransaction> findLatestTransactions();

    // 2. Get transaction history for a customer
    @Query("SELECT t FROM BankTransaction t " +
           "WHERE t.sender.id = :customerId OR t.receiver.id = :customerId " +
           "ORDER BY t.transactionDate DESC")
    List<BankTransaction> findTransactionHistory(
            @Param("customerId") Long customerId
    );

    // 3. Find transactions by status
    @Query("SELECT t FROM BankTransaction t WHERE t.status = :status")
    List<BankTransaction> findByTransactionStatus(
            @Param("status") TransactionStatus status
    );

    // 4. Count transactions by status
    @Query("SELECT COUNT(t) FROM BankTransaction t WHERE t.status = :status")
    long countByTransactionStatus(
            @Param("status") TransactionStatus status
    );

    // 5. Task 12.1: Find successful transfers above a given amount
    @Query("SELECT t FROM BankTransaction t " +
           "WHERE t.amount > :minAmount " +
           "AND t.status = 'SUCCESS' " +
           "ORDER BY t.amount DESC")
    List<BankTransaction> findLargeTransfers(
            @Param("minAmount") BigDecimal minAmount
    );

    // 6. Task 12.2: Calculate total successful amount sent
    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
           "FROM BankTransaction t " +
           "WHERE t.sender.id = :customerId " +
           "AND t.status = 'SUCCESS'")
    BigDecimal getTotalAmountSent(
            @Param("customerId") Long customerId
    );
}
