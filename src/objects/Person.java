package objects;

import contracts.AbstractContract;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Person {
    private final String id;
    private final LegalForm legalForm;
    private int paidOutAmount;
    private final Set<AbstractContract> contracts;

    public Person(String id){
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }

        if(isValidBirthNumber(id)){
            legalForm= LegalForm.NATURAL;
        }else if(isValidRegistrationNumber(id)){
            legalForm= LegalForm.LEGAL;
        }else {
            throw new IllegalArgumentException("ID is not valid");
        }

        this.id = id;
        paidOutAmount=0;
        this.contracts = new LinkedHashSet<>();

    }

    public static boolean isValidBirthNumber(String birthNumber){
        if (birthNumber == null || (birthNumber.length() != 9 && birthNumber.length() != 10)) {
            return false;
        }

        for (char c : birthNumber.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        int RR = Integer.parseInt(birthNumber.substring(0, 2));
        int MM = Integer.parseInt(birthNumber.substring(2, 4));
        int DD = Integer.parseInt(birthNumber.substring(4, 6));

        int actualMonth = MM;
        if (MM > 50 && MM <= 62) {
            actualMonth = MM - 50;
        } else if (MM < 1 || MM > 12) {
            return false;
        }

        int year;
        if (birthNumber.length() == 9) {
            if (RR > 53) {
                return false;
            }
            year = 1900 + RR;
        } else {
            year = (RR < 54) ? 2000 + RR : 1900 + RR;

            int sum = 0;
            for (int i = 0; i < 10; i++) {
                int digit = Character.getNumericValue(birthNumber.charAt(i));
                sum += (i % 2 == 0 ? 1 : -1) * digit;
            }
            if (sum % 11 != 0) {
                return false;
            }
        }

        try {
            LocalDate date = LocalDate.of(year, actualMonth, DD);
            return !date.isAfter(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidRegistrationNumber(String registrationNumber){
        if(registrationNumber == null|| (registrationNumber.length() != 6 && registrationNumber.length() != 8) ){
            return false;
        }

        for (char c : registrationNumber.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public String getId() {
        return id;
    }

    public int getPaidOutAmount() {
        return paidOutAmount;
    }

    public LegalForm getLegalForm() {
        return legalForm;
    }

    public Set<AbstractContract> getContracts() {
        return contracts;
    }

    public void addContract(AbstractContract contract) {
        if (contract == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        contracts.add(contract);
    }

    public void payout(int paidOutAmount) {
        if (paidOutAmount <= 0) {
            throw new IllegalArgumentException("Paid out amount must be positive");
        }
        this.paidOutAmount += paidOutAmount;
    }
}
