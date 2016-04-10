/* ------------------------------------------------------------------------- */
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

package main.conn;

import main.ASN1.ASN1DecoderFail;
import main.ASN1.ASNObj;
import main.data.Events;
import main.data.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Class to handle each connected client to the server. This class runs on it's own thread.
 * The handler contains the SQLite Database object in a static field to be shared among all clients.
 */
public class TCPClientHandler extends Thread {

    /* Socket object of the connected client */
    private Socket mClient;

    /* Database object passed in from the Main class Shared between all threads */
    private static Parser mParser;

    /**
     * Create a handler object for a Client
     *
     * @param client - Socket of the connected client
     */
    public TCPClientHandler(Socket client, Parser parser) {
        this.mClient = client;

        // do not reassign the static variable if it is already set
        if (mParser == null) {
            mParser = parser;
        }

        mParser.setClientInfo(mClient.getLocalAddress().getHostAddress(), mClient.getLocalPort());
    }

    /**
     * The method that runs on a separate thread.
     * Here is where the communication between the server and the client processes.
     */
    public void run() {
        super.run();

        InputStream in;
        OutputStream out;
        try {
            in = mClient.getInputStream();
            out = mClient.getOutputStream();
        } catch (IOException e) {
            System.err.println("Unable to connect to intput/output stream.\n" + e.toString());
            return;
        }


        while (mClient.isConnected() && !mClient.isClosed()) {
            final byte inBytes[] = new byte[65536];
            int bytesRead;
            try {
                // read input from the client. (MUST end in a \n)
                bytesRead = in.read(inBytes);
            } catch (IOException e) {
                //System.err.println("Unable to read input stream.\n" + e.toString());
                break;
            }

            if (bytesRead == 0) {
                break;
            }

            // parse and process the command given by the client
            ASNObj asnObj;
            try {
                asnObj = Parser.processBytes(inBytes, bytesRead);
            } catch (ASN1DecoderFail asn1DecoderFail) {
                System.err.println("Unable to parse client ASN1");
                break;
            }

            ASNObj response = null;
            try {
                response = Parser.getAsnObjResponse(asnObj, mParser.getClientIP(), mParser.getClientPort());
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                // write back to the client. including the \n char
                if (response != null) {
                    out.write(response.encode());
                } else {
                    out.write(new Events.ProjectOK(-1).encode());
                }
                out.flush();
            } catch (IOException e) {
                //System.err.println("Unable to write to the output stream.\n" + e.toString());
                break;
            } catch (NullPointerException e) {
                // client dc
                break;
            }
        }

        try {
            // don't forget to close streams :)
            mClient.close();
            in.close();
            out.close();
        } catch (IOException e) {
            System.err.println("Unable to close client streams.\n" + e.toString());
        }
        System.out.println("Client disconnected: " + mClient.getLocalAddress().getHostAddress() + ":" + mClient.getPort());
    }
}
