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


}
