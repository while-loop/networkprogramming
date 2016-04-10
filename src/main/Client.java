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
import main.ASN1.ASN1DecoderFail;
import main.ASN1.ASNObj;
import main.ASN1.Decoder;
import main.data.Events;
import main.data.Parser;
import main.data.Project;
import main.data.Tags;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.text.ParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws ASN1DecoderFail {

        // check for command line arguments
        // assign default values udp mode and port
        int port = 4232;
        boolean isUDP = false;
        String address = "localhost";

        Getopt g = new Getopt("Client", args, "h:p:tu");
        int c;


        // if there are custom arguments, apply them to the program
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'h':
                    address = g.getOptarg();
                    break;
                case 'p':
                    port = Integer.parseInt(g.getOptarg());
                    break;
                case 'u':
                    isUDP = true;
                    break;
                case 't':
                    isUDP = false;
                    break;
            }
        }

        if (isUDP) {
            runUDP(address, port);
        } else {
            runTCP(address, port);
        }
    }

    private static void runTCP(String address, int port) throws ASN1DecoderFail {
        Socket conn = null;
        try {
            conn = new Socket(address, port);
        } catch (IOException e) {
            printAndExit("Unable to connect to the TCP server.\n" + e.toString());
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            in = conn.getInputStream();
            out = conn.getOutputStream();
        } catch (IOException e) {
            printAndExit("Unable to connect to intput/output stream.\n" + e.toString());
        }

        Scanner scan = new Scanner(System.in);
        String clientInput;
        while (conn.isConnected() && !conn.isClosed()) {

            try {
                clientInput = scan.nextLine();
            } catch (Exception e) {
                break;
            }
            // write to the client. including the \n char
            try {
                ASNObj asnObj = getAsnObj(clientInput);
                if (asnObj != null) {
                    out.write(asnObj.encode());
                    out.flush();
                } else {
                    System.err.println("unable to parse client ASN1");
                }
            } catch (NoSuchElementException e) {
                // program closed. close the streams
            } catch (SocketException e) {
                System.err.println("TCP Server connection closed");
            } catch (IOException e) {
                System.err.println("Unable to write to the output stream.\n" + e.toString());
            }


            // read input from the server. (MUST end in a \n)
            try {
                final byte inBytes[] = new byte[65536];
                final int bytesRead = in.read(inBytes);

                printServerResponse(inBytes, bytesRead);
            } catch (SocketException e) {
                //server closed
            } catch (IOException e) {
                System.err.println("Unable to read to the input stream.\n" + e.toString());

            }
        }


        // don't forget to close streams :)
        try {
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

    private static void runUDP(String address, int port) throws ASN1DecoderFail {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            printAndExit("Unable to create UDP socket.\n" + e.toString());
        }

        InetAddress IPAddress = null;
        try {
            System.out.println(address);
            System.out.println(port);
            IPAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            printAndExit("Unable to parse hostname.\n" + e.toString());
        }

        byte[] receiveData = new byte[65536];
        DatagramPacket recv, snd;
        Scanner scan = new Scanner(System.in);
        String input;

        while (true) {
            try {
                input = scan.nextLine();
            } catch (Exception e) {
                break;
            }
            if (input.equalsIgnoreCase("quit")) {
                break;
            }

            ASNObj asnObj = getAsnObj(input);
            if (asnObj == null) {
                System.err.println("unable to parse client ASN1");
                continue;
            }
            byte[] sendBuff = asnObj.encode();

            if (sendBuff.length > 65536) {
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

            printServerResponse(recv.getData(), recv.getLength());
        }

        if (socket != null) {
            socket.close();
        }
    }

    private static void printServerResponse(byte[] inBytes, int bytesRead) throws ASN1DecoderFail {
        Decoder dec = new Decoder(inBytes, 0, bytesRead);

        ASNObj serverResponse = null;
        if (dec.getTypeByte() == Tags.TAG_AC0) { // ProjectOK
            serverResponse = new Events.ProjectOK().decode(dec);
        } else if (dec.getTypeByte() == Tags.TAG_AC3) { // ProjectsAnswer
            serverResponse = new Events.ProjectsAnswer().decode(dec);
        }

        if (serverResponse != null) {
            System.out.println(serverResponse.toString());
        } else {
            System.err.println("Unable to parse server response.");
        }
    }


    private static void printAndExit(String err) {
        System.err.println(err);
        System.exit(0);
    }

    private static ASNObj getAsnObj(String clientInput) {
        String verb = Parser.getVerb(clientInput);
        ASNObj obj = null;
        switch (verb) {
            case Tags.GET_PROJECT_TAG:
                obj = new Events.GetProject(clientInput);
                break;
            case Tags.GET_PROJECTS_TAG:
                obj = new Events.Projects();
                break;
            case Tags.TAKE_TAG:
                obj = new Events.Take(clientInput);
                break;
            case Tags.PROJECT_DEFINITION_TAG:
                try {
                    obj = new Project(clientInput);
                } catch (ParseException e) {
                    System.err.println("Unable to parse " + Tags.PROJECT_DEFINITION_TAG);
                }
                break;
        }
        return obj;
    }
}
