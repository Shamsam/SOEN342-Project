package demo.src.railsystem;


public class Main {
    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        terminal.getLoader().loadConnections("demo/resources/eu_rail_network.csv");
    }
}
