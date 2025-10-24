package railsystem;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Traveller {
    private static final Map<Integer, Traveller> travellerRegistry = new ConcurrentHashMap<>();

    private String firstName;
    private String lastName;
    private int ID;

    private Traveller(String firstName, String lastName, int iD) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ID = iD;
    }

    public static Traveller getInstance(String firstName, String lastName, int iD) {
        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()
                || iD <= 0) {
            throw new IllegalArgumentException("First name and last name cannot be null or empty");
        }

        Traveller existingTraveller = travellerRegistry.get(iD);
        if (existingTraveller != null) {
            return existingTraveller;
        }

        Traveller newTraveller = new Traveller(firstName, lastName, iD);
        travellerRegistry.put(iD, newTraveller);
        return newTraveller;
    }

    public static Map<Integer, Traveller> getAllTravellers() {
        return Collections.unmodifiableMap(travellerRegistry);
    }

    public static void clearRegistry() {
        travellerRegistry.clear();
    }

}
