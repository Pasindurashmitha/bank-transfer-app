
package com.example.bank_transfer_app.controller;

import com.example.bank_transfer_app.dto.TransactionForm;
import com.example.bank_transfer_app.service.BankService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class TransactionPageController {

    private final BankService bankService;

    public TransactionPageController(BankService bankService) {
        this.bankService = bankService;
    }

    // Open Deposit Page
    @GetMapping("/deposit")
    public String showDepositPage(Model model) {
        model.addAttribute("transactionForm", new TransactionForm());
        return "deposit";
    }

    // Process Deposit
    @PostMapping("/deposit")
    public String deposit(
            @Valid @ModelAttribute("transactionForm")
            TransactionForm form,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "deposit";
        }

        try {
            bankService.deposit(
                    form.getAccountNumber(),
                    form.getAmount()
            );

            model.addAttribute(
                    "successMessage",
                    "Deposit completed successfully!"
            );

        } catch (RuntimeException exception) {
            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        model.addAttribute("transactionForm", new TransactionForm());
        return "deposit";
    }

    // Open Withdraw Page
    @GetMapping("/withdraw")
    public String showWithdrawPage(Model model) {
        model.addAttribute("transactionForm", new TransactionForm());
        return "withdraw";
    }

    // Process Withdrawal
    @PostMapping("/withdraw")
    public String withdraw(
            @Valid @ModelAttribute("transactionForm")
            TransactionForm form,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "withdraw";
        }

        try {
            bankService.withdraw(
                    form.getAccountNumber(),
                    form.getAmount()
            );

            model.addAttribute(
                    "successMessage",
                    "Withdrawal completed successfully!"
            );

        } catch (RuntimeException exception) {
            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        model.addAttribute("transactionForm", new TransactionForm());
        return "withdraw";
    }
}
