package service;

public class RiskAnalysis {
    private final double riskScore;
    private final boolean flagged;
    private final String reason;

    public RiskAnalysis(double riskScore, boolean flagged, String reason) {
        this.riskScore = riskScore;
        this.flagged = flagged;
        this.reason = reason;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public String getReason() {
        return reason;
    }
}