package hhww;
import java.io.*;
import java.net.*;

public class UDPServer {
    private static final String DATABASE_FILE = "database.txt";
    private static final String CREDENTIALS_FILE = "credentials.txt";

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(9876)) {
            System.out.println("UDP Server started...");
            byte[] receiveBuffer = new byte[1024];
            byte[] sendBuffer;

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);
                String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                String response;
                if (clientMessage.startsWith("LOGIN:")) {
                    response = authenticate(clientMessage.substring(6).trim()) ? "Login successful." : "Invalid credentials.";
                } else {
                    response = "Unknown command.";
                }

                sendBuffer = response.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
                serverSocket.send(sendPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean authenticate(String credentials) throws IOException {
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
}
	