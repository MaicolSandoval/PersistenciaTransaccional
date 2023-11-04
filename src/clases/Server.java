package clases;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/*
 * @autor grupo ## poli  persistencia transaccional
 */

public class Server {
    /*
     * creando el socket para el servidor 
     */
    private static ServerSocket serverSocket = null;

    
    private static ArrayList<ClientThread> clientsConnected = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("----- SERVIDOR DEL GRUPO POLI -----");

        int numPort = 12345;

        if (args.length > 0) {
            numPort = Integer.parseInt(args[0]);
            System.out.println("El servidor usará el puerto asignado: " + numPort);
        } else {
            System.out.println("No se especificó el puerto. El servidor usará el puerto " + numPort + " por defecto.");
        }

        try {
            serverSocket = new ServerSocket(numPort);
        } catch (IOException e) {
            System.out.println("No se pudo crear el socket en el servidor.");
            System.exit(1);
        }

        int clientNum = 1;

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(clientSocket, clientsConnected, clientNum);
                clientsConnected.add(clientThread);
                clientThread.start();
                System.out.println("Cliente " + clientNum + " se ha conectado :)");
                clientNum++;
            } catch (IOException e) {
                System.out.println("No se pudo establecer la conexión con el cliente.");
            }
        }
    }

    static class ClientThread extends Thread {
        private String clientName;
        private ObjectInputStream is;
        private DataOutputStream os;
        private Socket clientSocket;
        private final ArrayList<ClientThread> clients;

        public ClientThread(Socket clientSocket, ArrayList<ClientThread> clients, int clientNum) {
            this.clientSocket = clientSocket;
            this.clients = clients;
            this.clientName = "Cliente" + clientNum;
        }

        public void run() {
            try {
                is = new ObjectInputStream(clientSocket.getInputStream());
                os = new DataOutputStream(clientSocket.getOutputStream());

                os.writeUTF("Por favor, ingrese su nombre:");
                String name = is.readUTF();

                if (name.indexOf('@') == -1 || name.indexOf('!') == -1) {
                    this.clientName = name;
                } else {
                    os.writeUTF("El nombre no debe contener los caracteres '@' o '!' ");
                }

                os.writeUTF("¡BIENVENIDO!\nEscribe 'chao' para salir.");
                os.writeUTF("Registro de cliente creado");

                synchronized (this) {
                    for (ClientThread currClient : clients) {
                        if (currClient != null && currClient != this) {
                            currClient.os.writeUTF(name + " se ha unido");
                        }
                    }
                }

                while (true) {
                    os.writeUTF("Escribe tu mensaje:");
                    String line = is.readUTF();

                    if (line.startsWith("#clientes")) {
                        StringBuilder clientList = new StringBuilder();
                        for (ClientThread currClient : clients) {
                            if (currClient != null && currClient.clientName != null) {
                                clientList.append(currClient.clientName).append(" ");
                            }
                        }
                        os.writeUTF("Clientes conectados: " + clientList.toString());
                    } else if (line.equals("chao")) {
                        break;
                    } else if (line.startsWith("@")) {
                        unicast(line, name);
                    } else {
                        broadcast(line, name);
                    }
                }
            } catch (IOException e) {
                System.out.println("La sesión del cliente se ha terminado.");
            }
        }

        void broadcast(String line, String name) throws IOException {
            synchronized (this) {
                for (ClientThread currClient : clients) {
                    if (currClient != null && currClient.clientName != null) {
                        currClient.os.writeUTF("<" + name + "> " + line);
                    }
                }
                os.writeUTF("Mensaje a todos los usuarios enviado.");
            }
        }

        void unicast(String line, String name) throws IOException {
            String[] words = line.split(":", 2);
            if (words.length > 1) {
                String recipientName = words[0].substring(1); // Remove '@' from recipient name
                String message = words[1].trim();
                for (ClientThread currClient : clients) {
                    if (currClient != null && currClient.clientName != null
                            && currClient.clientName.equals(recipientName)) {
                        currClient.os.writeUTF("<" + name + "> " + message);
                        os.writeUTF("Mensaje enviado a: " + currClient.clientName);
                        break;
                    }
                }
            }
        }
    }
}
