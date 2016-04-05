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

import main.data.Parser;

import java.io.*;
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
     * @param db     - database object for the connected client
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

        BufferedReader in;
        BufferedWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(mClient.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(mClient.getOutputStream()));
        } catch (IOException e) {
            System.err.println("Unable to connect to intput/output stream.\n" + e.toString());
            return;
        }


        String input = "", output;
        while (mClient.isConnected() && !mClient.isClosed()) {
            try {
                // read input from the client. (MUST end in a \n)
                input = in.readLine();
            } catch (IOException e) {
                System.err.println("Unable to read input stream.\n" + e.toString());
            }

            if (input == null) {
                break;
            }

            // parse and process the command given by the client
            output = mParser.processInput(input);

            try {
                // write back to the client. including the \n char
                out.write(output + "\n");
                out.flush();
            } catch (IOException e) {
                System.err.println("Unable to write to the output stream.\n" + e.toString());
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
