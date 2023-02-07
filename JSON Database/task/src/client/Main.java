package client;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;


public class Main {
    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 34522;
    private static final String INPUT_PATH =  "/Users/afeta/IdeaProjects/JSON Database/JSON Database/task/src/client/data/";

    private static void connect(String message) {
        try (
                Socket socket = new Socket(ADDRESS, PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ) {
            System.out.println("Client started!");

            output.writeUTF(message);
            System.out.println("Sent: " + message);

            message = input.readUTF();
            System.out.println("Received: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String createJSON(Arguments arguments) {
        Gson gson = new Gson();
        String json = "";
        if (arguments.getPath() == null) {
            json = gson.toJson(arguments);
        } else {
            try {
                json = new String(Files.readAllBytes(Path.of(INPUT_PATH + arguments.getPath())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    public static void main(String[] args) {
        Arguments arguments = new Arguments();
        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);
        String message = createJSON(arguments);

        connect(message);
    }
}
