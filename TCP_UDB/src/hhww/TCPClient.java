package hhww;

import java.io.*;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to TCP Server");

            // Login
            System.out.print(in.readLine() + " ");
            String loginCredentials = console.readLine();
            out.println(loginCredentials);

            String response = in.readLine();
            System.out.println(response);
            if (response.startsWith("Invalid")) return;

            // Handle requests
            String request;
            while (true) {
                System.out.print("Enter command: ");
                request = console.readLine();
                if (request.equalsIgnoreCase("exit")) break;
                out.println(request);
                System.out.println("Server Response: " + in.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
