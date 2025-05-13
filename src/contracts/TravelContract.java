package contracts;

import company.InsuranceCompany;
import objects.LegalForm;
import objects.Person;
import payment.ContractPaymentData;

import java.util.Set;

public class TravelContract extends AbstractContract{
    private final Set<Person> insuredPersons;

    public TravelContract(String contractNumber, InsuranceCompany insurer, Person policyHolder, ContractPaymentData contractPaymentData, int coverageAmount, Set<Person> personsToInsure) {
        super(contractNumber, insurer, policyHolder, contractPaymentData, coverageAmount);

        if (personsToInsure == null || personsToInsure.isEmpty()) {
            throw new IllegalArgumentException("personsToInsure must not be null or empty");
        }

        if (contractPaymentData == null) {
            throw new IllegalArgumentException("contractPaymentData must not be null");
        }

        for (Person person : personsToInsure) {
            if (person == null || person.getLegalForm() != LegalForm.NATURAL) {
                throw new IllegalArgumentException("personsToInsure contains a null element or is not a natural person");
            }
        }

        this.insuredPersons = personsToInsure;
    }

    public Set<Person> getInsuredPersons() {
        return insuredPersons;
    }
}
