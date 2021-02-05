import io.grpc.ServerBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class Server {
    // The standard TLS port is 443, but we use 8443 below to avoid needing extra permissions from the OS
    // Source: https://github.com/grpc/grpc-java/blob/master/SECURITY.md
    private static final int PORT = 8443;
    private io.grpc.Server server;

    public static void main(String[] args)
            throws InterruptedException, IOException {


        Server server = new Server();
        server.start();
        server.blockUntilShutdown();
    }

    public void start() throws IOException {
        try {
            DBUtils db = new DBUtils(System.getProperty("user.dir") + "/dbserver.db");

            server = ServerBuilder
                    .forPort(PORT)
                    .addService(new ServerService(db))
                    .useTransportSecurity(
                            new FileInputStream("certs/server.crt"),
                            new FileInputStream("certs/server_pkcs8.pem"))
                    .build()
                    .start();
            System.out.printf("Server is listening on port %d\n", PORT);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server == null) {
            return;
        }

        server.awaitTermination();
    }


}