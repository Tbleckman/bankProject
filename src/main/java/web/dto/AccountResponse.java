package web.dto;

import database.DatabaseManager.AccountData;

public record AccountResponse(
        String accountNumber,
        String bankName,
        String accountType,
        double balance,
        boolean active
) {

    public static AccountResponse from(AccountData a) {
        return new AccountResponse(a.accountNumber, bankNameFor(a.accountNumber), a.accountType, a.balance, a.isActive);
    }

    private static String bankNameFor(String accountNumber) {
        if (accountNumber.startsWith("BNY")) return "BNY Mellon";
        if (accountNumber.startsWith("CHS")) return "Chase";
        if (accountNumber.startsWith("CAP")) return "Capital One";
        return "Unknown";
    }
}
