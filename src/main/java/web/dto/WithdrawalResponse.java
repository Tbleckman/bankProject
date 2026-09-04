package web.dto;

import banking.AccountService.WithdrawalResult;

public record WithdrawalResponse(
        AccountResponse account,
        Double riskScore,
        Boolean flagged,
        String riskReason,
        boolean riskServiceAvailable
) {

    public static WithdrawalResponse from(WithdrawalResult result) {
        if (result.risk() == null) {
            // risk_service was unreachable - withdrawal still went through
            return new WithdrawalResponse(AccountResponse.from(result.account()), null, null, null, false);
        }
        return new WithdrawalResponse(
                AccountResponse.from(result.account()),
                result.risk().getRiskScore(),
                result.risk().isFlagged(),
                result.risk().getReason(),
                true
        );
    }
}
