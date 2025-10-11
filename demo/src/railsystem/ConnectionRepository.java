import java.util.List;
import java.util.stream.Collectors;

public class ConnectionRepository {
    private List<Connection> connections;

    public ConnectionRepository(List<Connection> connections) {
        this.connections = connections;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    public List<Connection> search(SearchCriteria criteria) {
        return connections.stream().filter(criteria::matches).collect(Collectors.toList());
    }
}
