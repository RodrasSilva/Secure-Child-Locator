import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DBUtils {


    String path;

    public DBUtils(String path) throws SQLException {
        this.path = "jdbc:sqlite:" + path;

        migrate();
    }

    private Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(this.path);
        return connection;
    }

    //    criar as tabelas
    private void migrate() throws SQLException {
        String guardians = "CREATE TABLE IF NOT EXISTS guardians("
                + "childId varchar(36) NOT NULL,"
                + "guardianId varchar(36) NOT NULL,"
                + "PRIMARY KEY (childId, guardianId)"
                + ");";

        String locations = "CREATE TABLE IF NOT EXISTS locations("
                + "id integer PRIMARY KEY AUTOINCREMENT,"
                + "childId varchar(36) NOT NULL references guardians(childId),"
                + "location text NOT NULL,"
                + "read integer NOT NULL"
                + ");";

        String logs = "CREATE TABLE IF NOT EXISTS request_logs("
                + "id integer PRIMARY KEY,"
                + "request_type text NOT NULL,"
                + "public_key blob NOT NULL"
                + ");";

        Connection connection = connect();
        Statement statement = connection.createStatement();

        statement.execute(guardians);
        statement.execute(locations);
        statement.execute(logs);

    }

    public void insertGuardian(String childId, String guardianId) throws SQLException {
        String query = "INSERT INTO guardians(childId, guardianId) VALUES(?, ?)";

        try (Connection connection = this.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, childId);
            statement.setString(2, guardianId);
            statement.executeUpdate();
        }
    }

    public void insertLocation(String childId, String location) throws SQLException {
        String query = "INSERT INTO locations(childId, location, read) VALUES(?,?,?)";

        try (Connection connection = this.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, childId);
            statement.setString(2, location);
            statement.setInt(3, 0);
            statement.executeUpdate();
        }
    }

    public String selectLastLocation(String guardianId, String childId) throws SQLException {
        String query = "SELECT location FROM locations WHERE childId = ? ORDER BY id DESC LIMIT 1";
        try (Connection connection = this.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, childId);
            ResultSet result = statement.executeQuery();
            if (result.next())
                return result.getString("location");
        }
        return null;
    }

    public List<String> selectAllLocations(String guardianId, String childId) throws SQLException {
        String query = "SELECT location FROM locations WHERE childId = ?";

        try (Connection connection = this.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, childId);
            ResultSet results = statement.executeQuery();

            List<String> locations = new ArrayList<>();
            while (results.next())
                locations.add(results.getString("location"));
            return locations;
        }
    }

    public List<String> updateLocations(String guardianId, String childId) throws SQLException {
        String query = "SELECT location FROM locations WHERE childId = ? and read= 0";

        try (Connection connection = this.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, childId);
            ResultSet results = statement.executeQuery();

            List<String> locations = new ArrayList<>();
            while (results.next())
                locations.add(results.getString("location"));
            return locations;
        }
    }

    public void addRequestLog(Connection connection, String requestType, byte[] publicKey) throws SQLException {
        String query_log = "INSERT INTO request_logs(request_type, public_key) VALUES(?, ?,?)";
        System.out.println("New log: " + requestType + ", pubK:" + publicKey);

        if (connection == null)
            connection = connect();

        PreparedStatement statement_log = connection.prepareStatement(query_log);
        statement_log.setString(1, requestType);
        statement_log.setBytes(2, publicKey);
        statement_log.executeUpdate();
    }
}