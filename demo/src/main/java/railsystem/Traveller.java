package railsystem;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Traveller {
    private static final Map<String, Traveller> travellerRegistry = new ConcurrentHashMap<>();

    private String firstName, lastName, id;
    private List<Ticket> tickets = new java.util.ArrayList<>();

    private Traveller(String firstName, String lastName, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
    }

    public static Traveller getInstance(String id) {
        Traveller existingTraveller = travellerRegistry.get(id);
        if (existingTraveller != null) {
            return existingTraveller;
        }
        throw new IllegalArgumentException("Traveller with ID '" + id + "' does not exist.");
    }

    public static Traveller getInstance(String firstName, String lastName, String id) {
        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()
                || id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("First name, last name, and/or id cannot be null or empty");
        }

        Traveller existingTraveller = travellerRegistry.get(id);
        if (existingTraveller != null) {
            return existingTraveller;
        }

        Traveller newTraveller = new Traveller(firstName, lastName, id);
        travellerRegistry.put(id, newTraveller);
        return newTraveller;
    }

    public static boolean exists(String id) {
        return travellerRegistry.containsKey(id);
    }

    public static Map<String, Traveller> getAllTravellers() {
        return Collections.unmodifiableMap(travellerRegistry);
    }

    public static void clearRegistry() {
        travellerRegistry.clear();
    }

}
