
package com.example.bank_transfer_app.repository;

import com.example.bank_transfer_app.entity.Customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 1. Find customer by account number
    @Query("SELECT c FROM Customer c WHERE c.accountNumber = :accountNumber")
    Optional<Customer> findByAccountNumber(
            @Param("accountNumber") String accountNumber
    );

    // 2. Count all customers
    @Query("SELECT COUNT(c) FROM Customer c")
    long countAllCustomers();

    // 3. Get customers ordered by balance (highest first)
    @Query("SELECT c FROM Customer c ORDER BY c.balance DESC")
    List<Customer> findCustomersOrderByBalance();

    // 4. Search customers by name or email
    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Customer> searchCustomers(
            @Param("keyword") String keyword
    );

    // 5. Calculate total balance of all customers
    @Query("SELECT COALESCE(SUM(c.balance), 0) FROM Customer c")
    BigDecimal getTotalBalance();

    // 6. Count customers using native SQL
    @Query(value = "SELECT COUNT(*) FROM customers", nativeQuery = true)
    long countCustomersNative();

    // 7. Debit customer account if sufficient balance is available
    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.balance = c.balance - :amount " +
           "WHERE c.accountNumber = :accountNumber AND c.balance >= :amount")
    int debitBalance(
            @Param("accountNumber") String accountNumber,
            @Param("amount") BigDecimal amount
    );

    // 8. Credit customer account
    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.balance = c.balance + :amount " +
           "WHERE c.accountNumber = :accountNumber")
    int creditBalance(
            @Param("accountNumber") String accountNumber,
            @Param("amount") BigDecimal amount
    );
}
