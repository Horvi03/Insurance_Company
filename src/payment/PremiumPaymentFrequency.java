package payment;

public enum PremiumPaymentFrequency {
    ANNUAL,
    SEMI_ANNUAL,
    QUARTERLY,
    MONTHLY;

    public int getValueInMonths(){
        switch (this) {
            case ANNUAL:
                return 12;
            case SEMI_ANNUAL:
                return 6;
            case QUARTERLY:
                return 3;
            case MONTHLY:
                return 1;
            default:
                throw new IllegalStateException("Unknown payment frequency");
        }
    }
}
