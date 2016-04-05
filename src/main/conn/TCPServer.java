package main.conn;

import main.data.Parser;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer extends Thread {

    Parser mParser;
    int mPort = 4232;

    public TCPServer(Parser parser, int port) {
        this.mParser = parser;
        this.mPort = port;
    }

    public void run() {
        super.run();

        ServerSocket server = null;
        try {
            server = new ServerSocket(mPort);
        } catch (IOException e) {
            System.out.println("Unable to start server.\r\n" + e.toString());
            return;
        }

        System.out.println("TCP server bound to: " + server.getInetAddress().getHostAddress() + ":" + server.getLocalPort());

        // main loop of the program
        while (server.isBound()) {
            try {
                Socket client = server.accept();
                System.out.println("Connected to: " + client.getLocalAddress().getHostAddress() + ":" + client.getPort());
                new TCPClientHandler(client, mParser).start();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        try {
            System.out.println("Closing TCP server.");
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
