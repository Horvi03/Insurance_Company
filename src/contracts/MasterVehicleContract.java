package contracts;

import company.InsuranceCompany;
import objects.LegalForm;
import objects.Person;

import java.util.LinkedHashSet;
import java.util.Set;

public class MasterVehicleContract extends AbstractVehicleContract {
    private final Set<SingleVehicleContract> childContracts;

    public MasterVehicleContract(String contractNumber, InsuranceCompany insurer, Person beneficiary, Person policyHolder){
        super(contractNumber, insurer, beneficiary, policyHolder, null ,0);
        if(policyHolder.getLegalForm() != LegalForm.LEGAL ){
            throw new IllegalArgumentException("Legal form should be LEGAL");
        }
        this.childContracts = new LinkedHashSet<>();
    }

    public Set<SingleVehicleContract> getChildContracts() {
        return childContracts;
    }

    public void requestAdditionOfChildContract(SingleVehicleContract contract) {
        if (contract == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        if (childContracts.contains(contract)) {
            throw new IllegalArgumentException("Contract already exists in the set");
        }
        if (!contract.getPolicyHolder().equals(this.policyHolder)) {
            throw new IllegalArgumentException("Child contract policy holder must be the same as master contract policy holder");
        }
        if (!contract.getInsurer().equals(this.insurer)) {
            throw new IllegalArgumentException("Child contract insurer must be the same as master contract insurer");
        }
        childContracts.add(contract);
    }

    @Override
    public boolean isActive() {
        if (childContracts.isEmpty()) {
            return super.isActive();
        }

        for (SingleVehicleContract contract : childContracts) {
            if (contract.isActive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setInactive() {
        for (SingleVehicleContract contract : childContracts) {
            contract.setInactive();
        }
        super.setInactive();
    }

    @Override
    public void pay(int amount) {
        getInsurer().getHandler().pay(this, amount);
    }

    @Override
    public void updateBalance() {
        getInsurer().chargePremiumOnContract(this);
    }


}
