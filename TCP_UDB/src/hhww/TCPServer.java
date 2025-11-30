package hhww;

import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServer {
    private static final String DATABASE_FILE = "database.txt";
    private static final String CREDENTIALS_FILE = "credentials.txt";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("TCP Server started...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private List<String> commandHistory = new ArrayList<>();

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ) {
                // Authentication
                out.println("LOGIN: Provide username and password.");
                String credentials = in.readLine();
                if (!authenticate(credentials)) {
                    out.println("Invalid credentials. Connection closing.");
                    return;
                }
                out.println("Login successful.");

                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    commandHistory.add(clientMessage);

                    if (clientMessage.startsWith("INQ:")) {
                        out.println(inquire(clientMessage.substring(4).trim()));
                    } else if (clientMessage.startsWith("ADD:")) {
                        out.println(addRecord(clientMessage.substring(4).trim()));
                    } else if (clientMessage.startsWith("DEL:")) {
                        out.println(deleteRecord(clientMessage.substring(4).trim()));
                    } else if (clientMessage.startsWith("UPD:")) {
                        out.println(updateRecord(clientMessage.substring(4).trim()));
                    } else if (clientMessage.startsWith("HIST:")) {
                        out.println("Command History: " + String.join(", ", commandHistory));
                    } else {
                        out.println("Unknown command.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean authenticate(String credentials) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader(CREDENTIALS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals(credentials)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private String inquire(String studentId) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader(DATABASE_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(studentId + " ")) {
                        return line;
                    }
                }
            }
            return "Student's record is not found.";
        }

        private String addRecord(String record) throws IOException {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATABASE_FILE, true))) {
                writer.write(record);
                writer.newLine();
            }
            return "Record added successfully.";
        }

        private String deleteRecord(String studentId) throws IOException {
            File tempFile = new File("temp.txt");
            boolean found = false;

            try (
                BufferedReader reader = new BufferedReader(new FileReader(DATABASE_FILE));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(studentId + " ")) {
                        found = true;
                    } else {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }

            tempFile.renameTo(new File(DATABASE_FILE));
            return found ? "Record deleted successfully." : "Student ID is not found.";
        }

        private String updateRecord(String updateInfo) throws IOException {
            String[] parts = updateInfo.split(":", 2);
            String studentId = parts[0].trim();
            String newRecord = parts[1].trim();

            File tempFile = new File("temp.txt");
            boolean found = false;

            try (
                BufferedReader reader = new BufferedReader(new FileReader(DATABASE_FILE));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(studentId + " ")) {
                        writer.write(newRecord);
                        found = true;
                    } else {
                        writer.write(line);
                    }
                    writer.newLine();
                }
            }

            tempFile.renameTo(new File(DATABASE_FILE));
            return found ? "Record updated successfully." : "Student ID is not found.";
        }
    }
}
