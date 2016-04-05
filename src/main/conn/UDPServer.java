package main.conn;

import main.data.Parser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer extends Thread {

    Parser mParser;
    int mPort = 4232;

    public UDPServer(Parser parser, int port) {
        this.mParser = parser;
        this.mPort = port;
    }

    public void run() {
        super.run();

        DatagramSocket server;
        try {
            server = new DatagramSocket(mPort);
        } catch (IOException e) {
            System.out.println("Unable to start server.\r\n" + e.toString());
            return;
        }

        System.out.println("UDP server bound to: " + "localhost:" + mPort);

        byte[] buf = new byte[1024];
        byte[] output;
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        String input = null;
        // main loop of the program
        while (server.isBound()) {
            try {
                server.receive(recv);
                input = new String(recv.getData(), 0, recv.getLength());
            } catch (IOException e) {
                System.err.println("Unable to receive packet.\n" + e.toString());
            }

            InetAddress IPAddress = recv.getAddress();
            int port = recv.getPort();

            output = this.mParser.processInput(input, IPAddress.getHostAddress(), port).getBytes();

            try {
                server.send(new DatagramPacket(output, output.length, IPAddress, port));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        System.out.println("Closing UDP server.");
        server.close();
    }
}
