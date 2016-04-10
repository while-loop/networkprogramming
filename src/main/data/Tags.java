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

package main.data;

import main.ASN1.Encoder;

/**
 * Class to keep constants
 */
public class Tags {

    /*
        Data identifiers
     */
    public static final String PROJECT_DEFINITION_TAG = "PROJECT_DEFINITION";
    public static final String PROJECT_TAG = "PROJECT";
    public static final String PROJECTS_TAG = "PROJECTS";
    public static final String TASKS_TAG = "TASKS";
    public static final String USER_TAG = "USER";

    /*
        Verbs
     */
    public static final String GET_PROJECTS_TAG = "GET_PROJECTS";
    public static final String GET_PROJECT_TAG = "GET_PROJECT";
    public static final String TAKE_TAG = "TAKE";
    public static final String WAITING_TAG = "Waiting";
    public static final String DONE_TAG = "Done";

    /*
        Responses
     */
    public static final String OK_TAG = "OK";
    public static final String FAIL_TAG = "FAIL";

    public static final String SD_FORMAT = "yyyy-MM-dd:hh'h'mm'm'ss's'SSS'Z'";


    public static final byte TAG_AP0 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_PRIMITIVE, (byte) 0);
    public static final byte TAG_AP1 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_PRIMITIVE, (byte) 1);
    public static final byte TAG_AP2 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_PRIMITIVE, (byte) 2);
    public static final byte TAG_AP3 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_PRIMITIVE, (byte) 3);
    public static final byte TAG_AP4 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_PRIMITIVE, (byte) 4);
    public static final byte TAG_AP5 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_PRIMITIVE, (byte) 5);
    public static final byte TAG_AP6 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_PRIMITIVE, (byte) 6);
    public static final byte TAG_AP7 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_PRIMITIVE, (byte) 7);
    public static final byte TAG_AP8 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_PRIMITIVE, (byte) 8);
    public static final byte TAG_AP9 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_PRIMITIVE, (byte) 9);

    public static final byte TAG_AC0 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 0);
    public static final byte TAG_AC1 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 1);
    public static final byte TAG_AC2 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 2);
    public static final byte TAG_AC3 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 3);
    public static final byte TAG_AC4 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 4);
    public static final byte TAG_AC5 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 5);
    public static final byte TAG_AC6 = asn1Type(Encoder.CLASS_APPLICATION, Encoder.PC_CONSTRUCTED, (byte) 6);

    public static byte asn1Type(int classASN1, int PCASN1, byte tag_number) {
        if ((tag_number & 0x1F) >= 31) {
            tag_number = 25;
        }
        return (byte) ((classASN1 << 6) + (PCASN1 << 5) + tag_number);
    }

}
