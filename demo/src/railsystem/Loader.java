import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public final class Loader {

    public List<Connection> loadConnections(Path csvPath) {
        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            return parse(br);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read: " + csvPath, e);
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                int adjustment = handleQuotedChar(line, i, c, sb);
                i += adjustment; // Add the adjustment instead of direct assignment
                if (c == '\"' && (i + 1 >= line.length() || line.charAt(i + 1) != '\"')) {
                    inQuotes = false;
                }
            } else {
                inQuotes = handleUnquotedChar(c, sb, out, inQuotes);
            }
        }
        out.add(sb.toString());
        return out;
    }

    private static int handleQuotedChar(String line, int i, char c, StringBuilder sb) {
        if (c == '\"' && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
            sb.append('\"');
            return 1; // Return adjustment value
        } else if (c != '\"') {
            sb.append(c);
        }
        return 0; // No adjustment
    }

    private static boolean handleUnquotedChar(char c, StringBuilder sb, List<String> out, boolean inQuotes) {
        if (c == ',') {
            out.add(sb.toString());
            sb.setLength(0);
            return inQuotes;
        } else if (c == '\"') {
            return true;
        } else {
            sb.append(c);
            return inQuotes;
        }
    }

    private static Set<DayOfWeek> parseSchedule(String raw) {
        String s = raw.trim().replaceAll("\\s+", "");
        if (s.equalsIgnoreCase("Daily")) {
            return EnumSet.allOf(DayOfWeek.class);
        }

        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        if (s.isEmpty())
            return days;

        // Allow "Mon,Wed,Fri" and ranges like "Fri-Sun"
        String[] parts = s.split(",");
        for (String part : parts) {
            if (part.isEmpty())
                continue;
            if (part.contains("-")) {
                String[] range = part.split("-");
                if (range.length == 2) {
                    DayOfWeek start = parseDOW(range[0]);
                    DayOfWeek end = parseDOW(range[1]);
                    addInclusiveRange(days, start, end);
                }
            } else {
                days.add(parseDOW(part));
            }
        }
        return days;
    }

    private static void addInclusiveRange(EnumSet<DayOfWeek> set, DayOfWeek start, DayOfWeek end) {
        DayOfWeek cur = start;
        while (true) {
            set.add(cur);
            if (cur == end)
                break;
            cur = cur.plus(1);
        }
    }

    private static DayOfWeek parseDOW(String abbrev) {
        String a = abbrev.toLowerCase();
        switch (a) {
            case "mon":
                return DayOfWeek.MONDAY;
            case "tue":
                return DayOfWeek.TUESDAY;
            case "wed":
                return DayOfWeek.WEDNESDAY;
            case "thu":
                return DayOfWeek.THURSDAY;
            case "fri":
                return DayOfWeek.FRIDAY;
            case "sat":
                return DayOfWeek.SATURDAY;
            case "sun":
                return DayOfWeek.SUNDAY;
            default:
                throw new IllegalArgumentException("Unrecognized day: " + abbrev);
        }
    }

    private static Object[] parseArrTime(String raw) {
        String s = raw.trim();
        if (s.endsWith("(+1d)")) {
            String timePart = s.substring(0, s.length() - 5).trim();
            LocalTime time = LocalTime.parse(timePart);
            return new Object[] { time, true };
        } else {
            LocalTime time = LocalTime.parse(s);
            return new Object[] { time, false };
        }
    }

    private List<Connection> parse(BufferedReader br) throws IOException {
        List<Connection> parsedConnections = new ArrayList<>();
        String line;

        // Read header (and ignore)
        String header = br.readLine();
        if (header == null)
            return parsedConnections;

        while ((line = br.readLine()) != null) {

            if (line.isBlank() || parseCsvLine(line).size() < 9) {
                continue;
            }

            List<String> cols = parseCsvLine(line);

            for (int i = 0; i < cols.size(); i++) {
                cols.set(i, cols.get(i).trim());
            }

            String parsedRouteId = cols.get(0);
            String parsedDepCity = cols.get(1);
            String parsedArrCity = cols.get(2);
            LocalTime parsedDepartTime = LocalTime.parse(cols.get(3)); // HH:mm
            Object[] arrTimeResult = parseArrTime(cols.get(4)); // HH:mm (+1d)
            LocalTime parsedArrivalTime = (LocalTime) arrTimeResult[0];
            boolean isNextDay = (Boolean) arrTimeResult[1];
            String parsedTrainType = cols.get(5);
            Set<DayOfWeek> parsedSchedule = parseSchedule(cols.get(6));
            BigDecimal parsedFirstClass = new BigDecimal(cols.get(7));
            BigDecimal parsedSecondClass = new BigDecimal(cols.get(8));

            parsedConnections.add(new Connection(parsedRouteId, parsedTrainType, parsedSchedule, parsedFirstClass,
                    parsedSecondClass, parsedDepCity, parsedDepartTime, parsedArrCity, parsedArrivalTime, isNextDay));
        }
        return parsedConnections;
    }

}
