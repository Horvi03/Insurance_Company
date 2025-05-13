package company;

import contracts.*;
import objects.Person;
import objects.Vehicle;
import payment.ContractPaymentData;
import payment.PaymentHandler;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Handler;

public class InsuranceCompany {
    private final Set<AbstractContract> contracts;
    private final PaymentHandler handler;
    private LocalDateTime currentTime;

    public InsuranceCompany(LocalDateTime currentTime) {
        if(currentTime==null){
            throw new IllegalArgumentException("Current time cannot be null.");
        }
        this.currentTime = currentTime;
        this.contracts = new LinkedHashSet<AbstractContract>();
        this.handler = new PaymentHandler(this);
    }

    public LocalDateTime getCurrentTime(){
        return currentTime;
    }

    public void setCurrentTime(LocalDateTime currentTime){
        if(currentTime==null){
            throw new IllegalArgumentException("CurrentTime cannot be null.");
        }
        this.currentTime = currentTime;
    }

    public Set<AbstractContract> getContracts() {
        return contracts;
    }

    public PaymentHandler getHandler() {
        return handler;
    }

    public SingleVehicleContract insureVehicle(String contractNumber, Person beneficiary, Person policyHolder, int proposedPremium, PremiumPaymentFrequency proposedPaymentFrequency, Vehicle vehicleToInsure){
        if(vehicleToInsure == null){
            throw new IllegalArgumentException("Vehicle to insure cannot be null.");
        }

        if(proposedPaymentFrequency == null){
            throw new IllegalArgumentException("Proposed payment frequency cannot be null.");
        }

        if(proposedPremium <= 0){
            throw new IllegalArgumentException("Proposed premium must be positive.");
        }

        for(AbstractContract contract : contracts){
            if(contract.getContractNumber().equals(contractNumber)){
                throw new IllegalArgumentException("Contract number already exists.");
            }
        }

        double annualPremium = (double)proposedPremium * (12 / proposedPaymentFrequency.getValueInMonths());

        if(annualPremium < (vehicleToInsure.getOriginalValue()*0.02)){
            throw new IllegalArgumentException("Proposed payment cannot be lower than 2% of original value");
        }

        int coverageAmount=vehicleToInsure.getOriginalValue()/2;

        ContractPaymentData contractPaymentData = new ContractPaymentData(proposedPremium, proposedPaymentFrequency, getCurrentTime(),0);

        SingleVehicleContract singleVehicleContract = new SingleVehicleContract(contractNumber,this, beneficiary, policyHolder, contractPaymentData, coverageAmount, vehicleToInsure);

        chargePremiumOnContract(singleVehicleContract);

        contracts.add(singleVehicleContract);
        policyHolder.addContract(singleVehicleContract);

        return singleVehicleContract;
    }

    public TravelContract insurePersons(String contractNumber, Person policyHolder, int proposedPremium, PremiumPaymentFrequency proposedPaymentFrequency, Set<Person> personsToInsure) {
        if(personsToInsure == null || personsToInsure.isEmpty()){
            throw new IllegalArgumentException("Persons to insure cannot be null or empty.");
        }
        if(proposedPaymentFrequency == null){
            throw new IllegalArgumentException("Proposed payment frequency cannot be null.");
        }
        if(proposedPremium <= 0){
            throw new IllegalArgumentException("Proposed premium must be positive.");
        }

        for(AbstractContract contract : contracts){
            if(contract.getContractNumber().equals(contractNumber)){
                throw new IllegalArgumentException("Contract number already exists.");
            }
        }

        int paymentsPerYear = 12 / proposedPaymentFrequency.getValueInMonths();
        int annualPremium = proposedPremium * paymentsPerYear;

        if(annualPremium < 5 * personsToInsure.size()) {
            throw new IllegalArgumentException("Annual premium must be at least 5 times the number of insured persons.");
        }

        ContractPaymentData contractPaymentData = new ContractPaymentData(proposedPremium, proposedPaymentFrequency, getCurrentTime(), 0);

        int coverageAmount = personsToInsure.size() * 10;

        TravelContract travelContract = new TravelContract(contractNumber, this,policyHolder, contractPaymentData, coverageAmount, personsToInsure);

        chargePremiumOnContract(travelContract);

        contracts.add(travelContract);
        policyHolder.addContract(travelContract);

        return travelContract;
    }

    public MasterVehicleContract createMasterVehicleContract(String contractNumber, Person beneficiary, Person policyHolder){
        for(AbstractContract contract : contracts){
            if(contract.getContractNumber().equals(contractNumber)){
                throw new IllegalArgumentException("Contract number already exists.");
            }
        }

        MasterVehicleContract masterVehicleContract = new MasterVehicleContract(contractNumber, this, beneficiary, policyHolder);

        contracts.add(masterVehicleContract);
        policyHolder.addContract(masterVehicleContract);

        return masterVehicleContract;
    }

    public void moveSingleVehicleContractToMasterVehicleContract(MasterVehicleContract masterVehicleContract, SingleVehicleContract singleVehicleContract) throws InvalidContractException {
        if (masterVehicleContract == null || singleVehicleContract == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }

        if (!contracts.contains(masterVehicleContract) || !contracts.contains(singleVehicleContract)) {
            throw new InvalidContractException("Contracts must be registered with this company");
        }

        if (!masterVehicleContract.isActive() || !singleVehicleContract.isActive()) {
            throw new InvalidContractException("Both contracts must be active");
        }

        if (!masterVehicleContract.getPolicyHolder().equals(singleVehicleContract.getPolicyHolder())) {
            throw new InvalidContractException("Both contracts must have the same policy holder");
        }

        if(!this.contracts.contains(singleVehicleContract) ||
                !singleVehicleContract.getPolicyHolder().getContracts().contains(singleVehicleContract) ||
                !this.contracts.contains(masterVehicleContract) ||
                !masterVehicleContract.getPolicyHolder().getContracts().contains(masterVehicleContract)){
            throw new InvalidContractException("contract is not valid.");
        }

        contracts.remove(singleVehicleContract);

        singleVehicleContract.getPolicyHolder().getContracts().remove(singleVehicleContract);

        masterVehicleContract.requestAdditionOfChildContract(singleVehicleContract);
    }

    public void chargePremiumsOnContracts(){
        for (AbstractContract contract : contracts) {
            if(contract.isActive()){
                contract.updateBalance();
            }
        }
    }

    public void chargePremiumOnContract(MasterVehicleContract contract){
        for (AbstractContract childContract : contract.getChildContracts()) {
            chargePremiumOnContract(childContract);
        }
    }

    public void chargePremiumOnContract(AbstractContract contract){
        if(contract == null){
            throw new IllegalArgumentException("Contract cannot be null");
        }

        if(contract.isActive()){
            LocalDateTime nextPaymentTime = contract.getContractPaymentData().getNextPaymentTime();
            ContractPaymentData paymentData = contract.getContractPaymentData();

            while(getCurrentTime().isEqual(nextPaymentTime) || getCurrentTime().isAfter(nextPaymentTime)){
                paymentData.setOutstandingBalance(
                        paymentData.getOutstandingBalance() + paymentData.getPremium()
                );

                paymentData.updateNextPaymentTime();

                nextPaymentTime = paymentData.getNextPaymentTime();
            }
        }
    }

    public void processClaim(TravelContract travelContract, Set<Person> affectedPersons){
        if(travelContract == null){
            throw new IllegalArgumentException("Contract cannot be null");
        }

        if(affectedPersons == null || affectedPersons.isEmpty()){
            throw new IllegalArgumentException("Affected persons cannot be null or empty");
        }

        if(!travelContract.getInsuredPersons().containsAll(affectedPersons)){
            throw new IllegalArgumentException("Person is not insured under this contract");
        }

        if(!travelContract.isActive()){
            throw new InvalidContractException("Contract is not active");
        }

        int payoutPerPerson = travelContract.getCoverageAmount() / affectedPersons.size();

        for(Person person : affectedPersons) {
            person.payout(payoutPerPerson);
        }
        travelContract.setInactive();
    }

    public void processClaim(SingleVehicleContract singleVehicleContract, int expectedDamages) {
        if(singleVehicleContract == null) {
            throw new IllegalArgumentException("Single vehicle contract cannot be null.");
        }

        if(expectedDamages <= 0) {
            throw new IllegalArgumentException("Expected damage cannot be lesser than 0.");
        }

        if(!singleVehicleContract.isActive()) {
            throw new InvalidContractException("Single vehicle contract is not active.");
        }

        Person payoutRecipient = singleVehicleContract.getBeneficiary();
        if (payoutRecipient == null) {
            payoutRecipient = singleVehicleContract.getPolicyHolder();
        }

        payoutRecipient.payout(singleVehicleContract.getCoverageAmount());

        if(expectedDamages >= (singleVehicleContract.getInsuredVehicle().getOriginalValue())*0.7) {
            singleVehicleContract.setInactive();
        }
    }
}

