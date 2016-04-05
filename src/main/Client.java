package main;/* ------------------------------------------------------------------------- */
/*   Copyright (C) 2016
                Author:  aalves2012@my.fit.edu
                Author:  stanyu2013@my.fit.edu
                Florida Tech, Computer Science

       This program is free software; you can redistribute it and/or modify
       it under the terms of the GNU Affero General Public License as published by
       the Free Software Foundation; either the current version of the License, or
       (at your option) any later version.

      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.

      You should have received a copy of the GNU Affero General Public License
      along with this program; if not, write to the Free Software
      Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.              */
/* ------------------------------------------------------------------------- */

import gnu.getopt.Getopt;

import java.io.*;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        // check for command line arguments
        // assign default values udp mode and port
        int port = 4232;
        boolean isUDP = false;

        Getopt g = new Getopt("Client", args, "p:u");
        int c;


        // if there are custom arguments, apply them to the program
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'p':
                    port = Integer.parseInt(g.getOptarg());
                    break;
                case 'u':
                    isUDP = true;
                    break;
            }
        }

        String address = "localhost";
        if (isUDP) {
            runUDP(address, port);
        } else {
            runTCP(address, port);
        }
    }

    private static void runTCP(String address, int port) {
        Socket conn = null;
        try {
            conn = new Socket(address, port);
        } catch (IOException e) {
            printAndExit("Unable to connect to the TCP server.\n" + e.toString());
        }

        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
        } catch (IOException e) {
            printAndExit("Unable to connect to intput/output stream.\n" + e.toString());
        }

        Scanner scan = new Scanner(System.in);
        String clientInput, serverOutput;
        while (conn.isConnected() && !conn.isClosed()) {

            try {
                // write back to the client. including the \n char
                clientInput = scan.nextLine();
                out.write(clientInput + "\n");
                out.flush();
            } catch (NoSuchElementException e) {
                // program closed. close the streams
                break;
            } catch (SocketException e) {
                System.err.println("TCP Server connection closed");
                break;
            } catch (IOException e) {
                System.err.println("Unable to write to the output stream.\n" + e.toString());
            }

            try {
                // read input from the server. (MUST end in a \n)
                serverOutput = in.readLine();
                System.out.println(serverOutput);
            } catch (SocketException e) {
                //server closed
                break;
            } catch (IOException e) {
                System.err.println("Unable to read to the input stream.\n" + e.toString());
            }
        }

        try {
            // don't forget to close streams :)
            conn.close();
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            System.err.println("Unable to close client streams.\n" + e.toString());
        }
    }

    private static void runUDP(String address, int port) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            printAndExit("Unable to create UDP socket.\n" + e.toString());
        }

        InetAddress IPAddress = null;
        try {
            IPAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            printAndExit("Unable to parse hostname.\n" + e.toString());
        }

        Scanner scan = new Scanner(System.in);
        byte[] sendBuff;
        byte[] receiveData = new byte[1024];
        String input = null;
        DatagramPacket recv, snd;

        while (true) {
			
            try {
				input = scan.nextLine();
			} catch (Exception e) {
				break;
			}
            if (input.equalsIgnoreCase("quit")) {
                break;
            }

            sendBuff = input.getBytes();

            if (sendBuff.length > 1024) {
                printAndExit("Buffer length greater than 1024");
            }

            snd = new DatagramPacket(sendBuff, sendBuff.length, IPAddress, port);
            try {
                socket.send(snd);
            } catch (IOException e) {
                System.err.println("Unable to send packet.\n" + e.toString());
            }

            recv = new DatagramPacket(receiveData, receiveData.length);
            try {
                socket.receive(recv);
            } catch (IOException e) {
                System.err.println("Unable to receive packet.\n" + e.toString());
            }

            System.out.println(new String(recv.getData(), 0, recv.getLength()));
        }

        if (socket != null) {
            socket.close();
        }
    }

    private static void printAndExit(String err) {
        System.err.println(err);
        System.exit(0);
    }
}
