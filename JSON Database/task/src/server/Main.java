package server;

import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    private static final int PORT = 34522;
    private static final String PATH = "/Users/afeta/IdeaProjects/JSON Database/JSON Database/task/src/server/data/db.json";
    private static final Database database = new Database(PATH);

    private static void connect() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started!");

            int poolSize = Runtime.getRuntime().availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(poolSize);

            while (true) {
                executor.submit(new Session(serverSocket, serverSocket.accept(), database));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        connect();
    }
}
