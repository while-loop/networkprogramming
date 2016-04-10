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

import main.conn.TCPServer;
import main.conn.UDPServer;
import main.data.Parser;
import main.db.MyDB;
import gnu.getopt.Getopt;

import java.io.File;

public class Server {

    public static void main(String[] args) {

        // assign default values to the database path and port
        String dbPath = new File("").getAbsolutePath() + File.separator + "np.sqlite";
        int port = 4232;

        Getopt g = new Getopt("Main", args, "p:d:");
        int c;

        // if there are custom arguments, apply them to the program
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'p':
                    port = Integer.parseInt(g.getOptarg());
                    break;
                case 'd':
                    dbPath = g.getOptarg();
                    break;
            }
        }


        Parser parser = null;
        try {
            parser = new Parser(new MyDB(dbPath));
        } catch (Exception e) {
            System.err.println("Unable to connect to database.\n" + e.toString());
            System.exit(0);
        }

        // start udp
        // start tcp
        TCPServer tcpServer = new TCPServer(parser, port);
        UDPServer udpServer = new UDPServer(parser, port);
        tcpServer.start();
        udpServer.start();

        try {
            tcpServer.join();
            udpServer.join();
        } catch (InterruptedException e) {
            System.err.println("Unable to join servers on main thread.\n" + e.toString());
        }
    }
}
