package main.conn;

import main.ASN1.ASNObj;
import main.data.Events;
import main.data.Parser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer extends Thread {

    private Parser mParser;
    private int mPort = 4232;

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
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        // main loop of the program
        while (server.isBound()) {
            ASNObj asnObj = null;
            try {
                server.receive(recv);
                asnObj = Parser.processBytes(recv.getData(), recv.getLength());
            } catch (Exception e) {
                System.err.println("Unable to receive packet.\n" + e.toString());
            }

            InetAddress IPAddress = recv.getAddress();
            int port = recv.getPort();

            ASNObj response = null;
            try {
                response = Parser.getAsnObjResponse(asnObj, IPAddress.getHostAddress(), port);
            } catch (Exception e) {
                System.err.println("Unable to parse ASN1 from client.\n" + e.toString());
            }
            try {
                if (response != null) {
                    byte[] data = response.encode();
                    server.send(new DatagramPacket(data, data.length, IPAddress, port));
                } else {
                    byte[] data = new Events.ProjectOK(-1).encode();
                    server.send(new DatagramPacket(data, data.length, IPAddress, port));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        System.out.println("Closing UDP server.");
        server.close();
    }
}
