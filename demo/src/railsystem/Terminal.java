final class Terminal {
    private static Terminal instance;
    private Loader loader;
    private ConnectionRepository connectionRepo;

    private Terminal() {
        this.loader = new Loader();
        this.connectionRepo = null;
    }

    public static Terminal getInstance() {
        if (instance == null) {
            instance = new Terminal();
        }
        return instance;
    }

    public Loader getLoader() {
        return loader;
    }

    public ConnectionRepository getConnectionRepo() {
        return connectionRepo;
    }

    public void setConnectionRepo(ConnectionRepository connectionRepo) {
        this.connectionRepo = connectionRepo;
    }
}
