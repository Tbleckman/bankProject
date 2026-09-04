package web.dto;

import database.DatabaseManager.Transaction;

public record TransactionResponse(
        int transactionId,
        String type,
        double amount,
        double balanceAfter,
        String description,
        String relatedAccount,
        String date
) {

    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.transactionId, t.type, t.amount, t.balanceAfter,
                t.description, t.relatedAccount,
                t.date == null ? null : t.date.toInstant().toString()
        );
    }
}
