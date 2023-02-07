package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Session implements Runnable {
    private Gson gson = new Gson();
    private ServerSocket server;
    private Socket socket;
    private Database database;

    public Session(ServerSocket server, Socket socket, Database database) {
        this.server = server;
        this.socket = socket;
        this.database = database;
    }

    private String makeResponse(String s) {
        JsonObject json = new JsonObject();
        if (s == null) {
            json.addProperty("response", "ERROR");
            json.addProperty("reason", "No such key");
        } else {
            json.addProperty("response", "OK");
            json.add("value", JsonParser.parseString(s));
        }
        return gson.toJson(json);
    }
    private String makeResponse(boolean b) {
        JsonObject json = new JsonObject();
        if (b) {
            json.addProperty("response", "OK");
        } else {
            json.addProperty("response", "ERROR");
            json.addProperty("reason", "No such key");
        }
        return gson.toJson(json);
    }
    private String processRequest(JsonObject request) {
        String command = request.get("type").getAsString();
        return switch (command) {
            case "set" -> makeResponse(database.set(request.get("key"), request.get("value")));
            case "get" -> makeResponse(database.get(request.get("key")));
            case "delete" -> makeResponse(database.delete(request.get("key")));
            default -> makeResponse(false);
        };
    }

    @Override
    public void run() {
        try (
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            String message = input.readUTF();
            System.out.println("Received: " + message);
            JsonObject request = JsonParser.parseString(message).getAsJsonObject();

            if (request.get("type").getAsString().equals("exit")) {
                message = makeResponse(true);
                output.writeUTF(message);
                System.out.println("Sent: " + message);
                server.close();
                System.exit(0);
            } else {
                message = processRequest(request);
                output.writeUTF(message);
                System.out.println("Sent: " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
