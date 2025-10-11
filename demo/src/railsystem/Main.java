import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Terminal terminal = Terminal.getInstance();
        List<Connection> connections = terminal.getLoader()
                .loadConnections(Paths.get("demo/resources/eu_rail_network.csv"));
        terminal.setConnectionRepo(new ConnectionRepository(connections));
        terminal.getConnectionRepo().getConnections().getFirst().printInfo();
    }
}
