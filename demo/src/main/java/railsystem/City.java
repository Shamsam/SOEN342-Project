package railsystem;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(of = "name")
public final class City {

    private static final Map<String, City> cityRegistry = new ConcurrentHashMap<>();

    private final String name;

    private City(String name) {
        this.name = name;
    }

    public static City getInstance(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be null or empty");
        }
        // computeIfAbsent ensures atomic creation
        return cityRegistry.computeIfAbsent(name.trim(), City::new);
    }

    public static boolean exists(String name) {
        return name != null && cityRegistry.containsKey(name.trim());
    }

    public static Map<String, City> getAllCities() {
        return Collections.unmodifiableMap(cityRegistry);
    }

    public static void clearRegistry() {
        cityRegistry.clear();
    }
}
