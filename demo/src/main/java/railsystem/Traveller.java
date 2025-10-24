package railsystem;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Traveller {
    private static final Map<String, Traveller> travellerRegistry = new ConcurrentHashMap<>();

    private String firstName;
    private String lastName;

    private Traveller(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static Traveller getInstance(String firstName, String lastName) {
        String key = firstName + " " + lastName;
        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name and last name cannot be null or empty");
        }

        Traveller existingTraveller = travellerRegistry.get(key);
        if (existingTraveller != null) {
            return existingTraveller;
        }

        Traveller newTraveller = new Traveller(firstName, lastName);
        travellerRegistry.put(key, newTraveller);
        return newTraveller;
    }

    public static Map<String, Traveller> getAllTravellers() {
        return Collections.unmodifiableMap(travellerRegistry);
    }

    public static void clearRegistry() {
        travellerRegistry.clear();
    }


}
