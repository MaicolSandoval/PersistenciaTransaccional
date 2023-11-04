package clases;

import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: java Client <nombre del servidor> <puerto>");
            System.exit(1);
        }

        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);

        try {
            Socket socket = new Socket(serverAddress, serverPort);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Conectado al servidor. Escribe tu nombre:");
            String clientName = reader.readLine();
            output.writeUTF(clientName);

            Thread inputThread = new Thread(() -> {
                try {
                    while (true) {
                        String message = input.readUTF();
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.err.println("Conexión con el servidor perdida.");
                    System.exit(1);
                }
            });
            inputThread.start();

            while (true) {
                String message = reader.readLine();
                output.writeUTF(message);
            }
        } catch (IOException e) {
            System.err.println("No se pudo conectar al servidor.");
            System.exit(1);
        }
    }
}

