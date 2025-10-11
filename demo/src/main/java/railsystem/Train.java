package railsystem;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(of = "trainType")
public final class Train {

    private static final Map<String, Train> trainRegistry = new ConcurrentHashMap<>();

    private final String trainType;

    private Train(String trainType) {
        this.trainType = trainType;
    }

    public static Train getInstance(String trainType) {
        if (trainType == null || trainType.trim().isEmpty()) {
            throw new IllegalArgumentException("Train type cannot be null or empty");
        }

        // computeIfAbsent is atomic — prevents race conditions
        return trainRegistry.computeIfAbsent(trainType.trim(), Train::new);
    }

    public static boolean exists(String trainType) {
        return trainType != null && trainRegistry.containsKey(trainType.trim());
    }

    public static Map<String, Train> getAllTrains() {
        return Collections.unmodifiableMap(trainRegistry);
    }

    public static void clearRegistry() {
        trainRegistry.clear();
    }
}
