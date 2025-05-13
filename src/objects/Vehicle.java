package objects;

public class Vehicle {
    private  final String licensePlate;
    private final int originalValue;

    public Vehicle(String licensePlate, int originalValue) {
        if(licensePlate == null || licensePlate.length() != 7 || !licensePlate.matches("[A-Z0-9]+")){
            throw new IllegalArgumentException("License plate is invalid");
        }
        if(originalValue <= 0){
            throw new IllegalArgumentException("Original value is invalid");
        }
        this.licensePlate = licensePlate;
        this.originalValue = originalValue;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public int getOriginalValue() {
        return originalValue;
    }
}
