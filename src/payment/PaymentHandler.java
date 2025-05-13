package payment;

import company.InsuranceCompany;
import contracts.AbstractContract;
import contracts.InvalidContractException;
import contracts.MasterVehicleContract;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PaymentHandler {
    private final Map<AbstractContract, Set<PaymentInstance>> paymentHistory;
    private final InsuranceCompany insurer;

    public PaymentHandler(InsuranceCompany insurer) {
        if(insurer == null) {
            throw new IllegalArgumentException("Insurance Company cannot be null");
        }
        this.insurer = insurer;
        this.paymentHistory = new HashMap<>();
    }

    public Map<AbstractContract,Set<PaymentInstance>> getPaymentHistory() {
        return paymentHistory;
    }

    public void pay(MasterVehicleContract contract, int amount) {
        if(contract == null || amount <= 0) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        if(!contract.isActive() || insurer != contract.getInsurer()){
            throw new InvalidContractException("Contract is not active");
        }
        if (contract.getChildContracts().isEmpty()) {
            throw new InvalidContractException("Contract has no child contracts");
        }

        int paymentAmount = amount;
        int remainingAmount = amount;

        for (AbstractContract childContract : contract.getChildContracts()) {
            if (!childContract.isActive()) {
                continue;
            }

            ContractPaymentData paymentData = childContract.getContractPaymentData();
            int outstandingBalance = paymentData.getOutstandingBalance();

            if (outstandingBalance > 0) {
                int paymentToApply = Math.min(remainingAmount, outstandingBalance);
                paymentData.setOutstandingBalance(outstandingBalance - paymentToApply);
                remainingAmount -= paymentToApply;

                if (remainingAmount == 0) {
                    break;
                }
            }
        }

        while (remainingAmount > 0) {
            boolean deducted = false;
            for (AbstractContract childContract : contract.getChildContracts()) {
                if (!childContract.isActive()) {
                    continue;
                }
                ContractPaymentData paymentData = childContract.getContractPaymentData();
                int premium = paymentData.getPremium();
                if (premium > 0) {
                    int paymentToApply = Math.min(remainingAmount, premium);
                    paymentData.setOutstandingBalance(paymentData.getOutstandingBalance() - paymentToApply);
                    remainingAmount -= paymentToApply;
                    deducted = true;
                    if (remainingAmount == 0) {
                        break;
                    }
                }
            }
            if (!deducted) {
                break;
            }
        }

        PaymentInstance paymentInstance = new PaymentInstance(insurer.getCurrentTime(), paymentAmount);
        paymentHistory.computeIfAbsent(contract, k -> new TreeSet<>()).add(paymentInstance);
    }

    public void pay(AbstractContract contract, int amount){
        if(contract == null || amount <= 0) {
            throw new IllegalArgumentException("Contract cannot be null");
        }

        if(!contract.isActive() || insurer != contract.getInsurer()) {
            throw new InvalidContractException("Contract is not active");
        }

        ContractPaymentData paymentData = contract.getContractPaymentData();
        paymentData.setOutstandingBalance(paymentData.getOutstandingBalance() - amount);

        PaymentInstance paymentInstance = new PaymentInstance(insurer.getCurrentTime(), amount);

        paymentHistory.computeIfAbsent(contract, k -> new TreeSet<>()).add(paymentInstance);
    }
}
