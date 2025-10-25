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
    private static final Map<Integer, Traveller> travellerRegistry = new ConcurrentHashMap<>();
    private static int idCount = 0;

    private String firstName;
    private String lastName;
    private int ID;
    private List<Ticket> tickets = new java.util.ArrayList<>();

    private Traveller(String firstName, String lastName, int id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ID = id;
    }

    public static Traveller getInstance(String firstName, String lastName, int id) {
        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()
                || id <= 0) {
            throw new IllegalArgumentException("First name and last name cannot be null or empty");
        }

        Traveller existingTraveller = travellerRegistry.get(id);
        if (existingTraveller != null) {
            return existingTraveller;
        }

        Traveller newTraveller = new Traveller(firstName, lastName, id);
        travellerRegistry.put(id, newTraveller);
        return newTraveller;
    }

    public static Map<Integer, Traveller> getAllTravellers() {
        return Collections.unmodifiableMap(travellerRegistry);
    }

    public static void clearRegistry() {
        travellerRegistry.clear();
    }

    public static int getIdCount() {
        return idCount;
    }

    public static int incrementIdCount() {
        idCount += 1;
        return idCount;
    }

}
