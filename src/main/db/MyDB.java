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

package main.db;

import main.data.Events;
import main.data.Project;
import main.data.Tags;
import main.data.Task;
import main.utils.CalHelper;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Database object to connect and interact with it.
 * Given a path to the file (in the constructor) it will create the file and tables, if needed.
 * <p>
 * To connect to the DB, call the connect() function.
 * To disconnect to the DB, call the disconnect() function.
 */
public class MyDB {
    /*  */
    Connection mConnection;
    String mDbPath;

    /* Table name constants */
    private static final String PROJECTS_TABLE = "projects";
    private static final String TASKS_TABLE = "tasks";

    /* Shared table column names */
    private static final String PROJECT_ID = "project_id";
    private static final String TITLE_ID = "title";

    /* Task table columns names */
    private static final String START_ID = "start";
    private static final String END_ID = "end";
    private static final String TASK_ID = "task_id";
    private static final String IP_ID = "ip";
    private static final String PORT_ID = "port";
    private static final String USER_ID = "user";
    private static final String STATUS_ID = "status";

    Timer dateChecker;


    /**
     * Create a database object
     *
     * @param dbPath - full path to a specific database file
     */
    public MyDB(String dbPath) {
        this.mDbPath = dbPath;
    }

    /**
     * Connect to a database given in the Class constructor. The java.db connection uses
     * JDBC SQLite.
     *
     * @throws SQLException - throws error if a connection to the database wasn't made
     * @throws IOException  - throws error if the database file wasn't created
     */
    public void connect() throws SQLException, IOException {
        File dbFile = new File(mDbPath);
        boolean createTables = false;
        if (!dbFile.exists()) {
            createFile(dbFile);
            createTables = true;
        }

        mConnection = DriverManager.getConnection("jdbc:sqlite:" + mDbPath);
        if (createTables) {
            createDB();
        }

        dateChecker = new Timer();
        dateChecker.schedule(new TimerTask() { // execute task every minute
            public void run() {
                try {
                    checkDate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 60000);
    }

    public void disconnect() {
        try {
            if (mConnection != null && !mConnection.isClosed()) {
                mConnection.close();
            }
            dateChecker.cancel();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a file is it doesn't exists. This method will create parent directories if needed.
     *
     * @param dbFile - file needed to be created on storage
     * @throws IOException - throws an error if the file was not created
     */
    private void createFile(File dbFile) throws IOException {
        // check if the parent dir exist
        // if not, we need to recursively create dirs to create the java.db file
        File parentDir = dbFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        if (!dbFile.createNewFile()) {
            throw new IOException("Unable to create Database file");
        }
    }

    /**
     * Creates a SQLite database file and the necessary tables to store
     * Project and Task information
     *
     * @throws SQLException - throws error if the creation of the tables were unsuccessful
     */
    private void createDB() throws SQLException {
        // Generate the commands to create the tables
        Statement statement = mConnection.createStatement();

        // projects table
        statement.executeUpdate("CREATE TABLE \"" + PROJECTS_TABLE + "\" (\n" +
                "\t`" + PROJECT_ID + "`\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                "\t`" + TITLE_ID + "`\tTEXT NOT NULL\n" +
                ")");

        // tasks table
        statement.executeUpdate("CREATE TABLE \"" + TASKS_TABLE + "\" (\n" +
                "\t`" + TASK_ID + "`\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                "\t`" + PROJECT_ID + "`\tINTEGER NOT NULL,\n" +
                "\t`" + TITLE_ID + "`\tTEXT,\n" +
                "\t`" + USER_ID + "`\tTEXT,\n" +
                "\t`" + START_ID + "`\tDATETIME,\n" +
                "\t`" + END_ID + "`\tDATETIME,\n" +
                "\t`" + IP_ID + "`\tTEXT,\n" +
                "\t`" + PORT_ID + "`\tINTEGER,\n" +
                "\t`" + STATUS_ID + "`\tTEXT,\n" +
                "\tFOREIGN KEY(`" + PROJECT_ID + "`) REFERENCES `" + PROJECTS_TABLE + "." + PROJECT_ID + "`\n" +
                ")");
        statement.close();
    }

    /**
     * DEPRECATED - use "Project getProject(Events.GetProject obj)"
     *
     * @param projectTitle - Title of the project to get information on
     * @return - String content containing information on the Project and any Tasks associated with it
     * @throws SQLException - raises exception if the SQL command was not able to execute
     */
    public String getProject(String projectTitle) throws SQLException {

        // check the java.db to update any task status if its end date has passed
        checkDate();

        /* Get the information about the Project. Tasks are selected later in the method */
        String sql = "SELECT *, (" +
                "SELECT count(*) " +
                "FROM " + TASKS_TABLE + " " +
                "WHERE " + TASKS_TABLE + "." + PROJECT_ID + " = " + PROJECTS_TABLE + "." + PROJECT_ID + ") AS task_size " +
                "FROM " + PROJECTS_TABLE + " " +
                "WHERE " + PROJECTS_TABLE + "." + TITLE_ID + " = ?";
        PreparedStatement ps = mConnection.prepareStatement(sql);
        ps.setString(1, projectTitle);
        ResultSet rs = ps.executeQuery();

        int taskSize = 0, projectId = -1;
        while (rs.next()) {
            taskSize = rs.getInt("task_size");
            projectId = rs.getInt(PROJECT_ID);
        }
        ps.close();
        rs.close();

        if (projectId == -1) {
            throw new SQLException("Unable to find project: " + projectTitle);
        }


        /* Get information on each task for the project */
        sql = "SELECT * " +
                "FROM " + TASKS_TABLE + " " +
                "WHERE " + PROJECT_ID + " = (SELECT " + PROJECT_ID + " FROM " + PROJECTS_TABLE + " WHERE " + TITLE_ID + " = ?)";

        ps = mConnection.prepareStatement(sql);
        ps.setString(1, projectTitle);
        rs = ps.executeQuery();

        // append each result task to the tasks string
        String tasks = "";
        while (rs.next()) {
            // title;start;end;user;ip;port;status
            tasks += ";" + rs.getString(TITLE_ID) + ";" + rs.getString(START_ID) +
                    ";" + rs.getString(END_ID) + ";" + rs.getString(USER_ID) +
                    ";" + rs.getString(IP_ID) + ";" + rs.getString(PORT_ID) +
                    ";" + rs.getString(STATUS_ID);
        }

        String result = Tags.PROJECT_DEFINITION_TAG + ":" + projectTitle + ";" +
                Tags.TASKS_TAG + ":" + taskSize + tasks;
        ps.close();
        rs.close();
        return result;
    }

    /**
     * Checks the tasks table for any rows that has an EndDate
     * that has already passed.
     *
     * @throws SQLException - raises exception if the statement was not able to execute
     */
    private void checkDate() throws SQLException {
        String sql = "UPDATE " + TASKS_TABLE + " " +
                "SET " + STATUS_ID + " = ? " +
                "WHERE " + END_ID + " <= DATETIME() AND " + STATUS_ID + " <> ?";
        PreparedStatement ps = mConnection.prepareStatement(sql);
        ps.setString(1, Tags.DONE_TAG);
        ps.setString(2, Tags.DONE_TAG);
        ps.executeUpdate();
    }

    /**
     * Adds a project to the database. The method will also insert the tasks associated with the
     * project.
     *
     * @param input - raw input given to the server from the client containing the project information
     * @throws ParseException - throws an exception when the input string is unable to be parsed
     * @throws SQLException   - throws an exception if the sql statement was unable to execute
     */
    public void addProject(String input) throws SQLException, ParseException {

        // create the pattern to parse the Project and Task information
        Pattern projectPattern = Pattern.compile(Tags.PROJECT_DEFINITION_TAG +
                ":(.*);" + Tags.TASKS_TAG + ":(\\d*);(.*;.*;.*;)*");

        // Now create matcher object and check if we were able to parse the input
        Matcher m = projectPattern.matcher(input);
        if (!m.find()) {
            System.out.println("No match.");
            throw new ParseException("Unable to parse input java.data.", 0);
        }

        // grab info about the project being inserted
        // Note: Tasks are contained in one string
        String title = m.group(1);
        int taskSize = Integer.parseInt(m.group(2));
        String tasks = m.group(3);

        // generate the SQL command to insert the Project
        String sql = "INSERT INTO " + PROJECTS_TABLE + " values(?, ?)";
        PreparedStatement ps = mConnection.prepareStatement(sql);
        // index #1 is the autoincrementing id
        ps.setString(2, title);         // title
        ps.executeUpdate();
        ps.close();

        // process the task list (if the project has tasks)
        if (taskSize > 0 && tasks != null) {
            // get the ID of the inserted project
            ps = mConnection.prepareStatement("SELECT " + PROJECT_ID + " FROM " + PROJECTS_TABLE + " WHERE " + TITLE_ID + " = ? ");
            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            int projectId = -1;
            while (rs.next()) {
                projectId = rs.getInt(PROJECT_ID);
            }
            ps.close();
            rs.close();

            // split the task java.data. task info [i]should[/i] be multiples of 3  Ex: (title;start;end)
            String[] tasksArray = tasks.split(";");
            int taskEntryLength = 3;
            for (int i = 0; i < taskSize; i++) {
                // Enter only part of the task. User info is set in a different command
                sql = "INSERT INTO " + TASKS_TABLE + " (" + PROJECT_ID + ", " + TITLE_ID +
                        ", " + START_ID + ", " + END_ID + ", " + STATUS_ID + ") VALUES (?, ?, ?, ?, ?)";
                ps = mConnection.prepareStatement(sql);
                ps.setInt(1, projectId);                                // project_id
                ps.setString(2, tasksArray[taskEntryLength * i + 0]);     // title
                ps.setString(3, tasksArray[taskEntryLength * i + 1]);     // start
                ps.setString(4, tasksArray[taskEntryLength * i + 2]);     // end
                ps.setString(5, Tags.WAITING_TAG);                      // status
                ps.executeUpdate();
                ps.close();
            }
        }
    }

    /**
     * Assign a user that is responsible for a task. The user info is
     * also stored into the database.
     *
     * @param user         - name of the user
     * @param projectTitle - title of the project to add the user
     * @param taskTitle    - title of the task to assign the user to
     * @param userIP       - IP address of the user connection
     * @param userPort     - port of the connection
     * @return - number of rows modified
     * @throws SQLException - throws exception if the update statement was unsuccessful
     */
    public int assignUser(String user, String projectTitle, String taskTitle, String userIP, int userPort) throws SQLException {
        String sql = "UPDATE " +
                TASKS_TABLE + " " +
                "SET " +
                USER_ID + " = ?, " +
                IP_ID + " = ?, " +
                PORT_ID + " = ?, " +
                STATUS_ID + " = ? " +
                "WHERE " +
                PROJECT_ID + " = (SELECT " + PROJECT_ID + " FROM " + PROJECTS_TABLE + " WHERE " + TITLE_ID + " = ?) AND " +
                TITLE_ID + " = ?";

        // sql escape using PreparedStatement... prevent sql injection
        PreparedStatement ps = mConnection.prepareStatement(sql);
        ps.setString(1, user);
        ps.setString(2, userIP);
        ps.setInt(3, userPort);
        ps.setString(4, Tags.WAITING_TAG);
        ps.setString(5, projectTitle);
        ps.setString(6, taskTitle);

        int rows = ps.executeUpdate();
        ps.close();
        return rows;
    }


    //////////////////////////////////////////////////////////////////////////////////////
    //                                    ASN1 Methods                                   //
    //////////////////////////////////////////////////////////////////////////////////////
    public Events.ProjectOK addProject(Project obj) throws ParseException, SQLException {

        String title = obj.getmName();
        ArrayList<Task> tasks = obj.getmTasks();

        // generate the SQL command to insert the Project
        String sql = "INSERT INTO " + PROJECTS_TABLE + " values(?, ?)";
        PreparedStatement ps = mConnection.prepareStatement(sql);
        // index #1 is the autoincrementing id
        ps.setString(2, title);         // title
        ps.executeUpdate();
        ps.close();

        // process the task list (if the project has tasks)
        if (tasks != null && tasks.size() > 0) {
            // get the ID of the inserted project
            ps = mConnection.prepareStatement("SELECT " + PROJECT_ID + " FROM " + PROJECTS_TABLE + " WHERE " + TITLE_ID + " = ? ");
            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            int projectId = -1;
            while (rs.next()) {
                projectId = rs.getInt(PROJECT_ID);
            }
            ps.close();
            rs.close();

            for (Task task : tasks) {
                // Enter only part of the task. User info is set in a different command
                sql = "INSERT INTO " + TASKS_TABLE + " (" + PROJECT_ID + ", " + TITLE_ID +
                        ", " + START_ID + ", " + END_ID + ", " + STATUS_ID + ") VALUES (?, ?, ?, ?, ?)";
                ps = mConnection.prepareStatement(sql);
                ps.setInt(1, projectId);                                // project_id
                ps.setString(2, task.getName());     // title
                ps.setString(3, CalHelper.getStringDate(task.getStart()));     // start
                ps.setString(4, CalHelper.getStringDate(task.getEnd()));     // end
                ps.setString(5, Tags.WAITING_TAG);                      // status
                ps.executeUpdate();
                ps.close();
            }
        }
        return new Events.ProjectOK(0, obj);
    }

    public Events.ProjectOK getProject(Events.GetProject obj) throws SQLException, ParseException {
        String projectTitle = obj.getmName();

        // check the java.db to update any task status if its end date has passed
        checkDate();

        /* Get the information about the Project. Tasks are selected later in the method */
        String sql = "SELECT *, (" +
                "SELECT count(*) " +
                "FROM " + TASKS_TABLE + " " +
                "WHERE " + TASKS_TABLE + "." + PROJECT_ID + " = " + PROJECTS_TABLE + "." + PROJECT_ID + ") AS task_size " +
                "FROM " + PROJECTS_TABLE + " " +
                "WHERE " + PROJECTS_TABLE + "." + TITLE_ID + " = ?";
        PreparedStatement ps = mConnection.prepareStatement(sql);
        ps.setString(1, projectTitle);
        ResultSet rs = ps.executeQuery();

        int projectId = -1;
        while (rs.next()) {
            projectId = rs.getInt(PROJECT_ID);
        }
        ps.close();
        rs.close();

        if (projectId == -1) {
            throw new SQLException("Unable to find project: " + projectTitle);
        }

        ArrayList<Task> tasks = getProjectTasks(projectId);

        return new Events.ProjectOK(0, new Project(projectTitle, tasks));
    }

    private ArrayList<Task> getProjectTasks(int projectId) throws SQLException, ParseException {
        /* Get information on each task for the project */
        String sql = "SELECT * FROM " + TASKS_TABLE + " " +
                "WHERE " + PROJECT_ID + " = ?";

        PreparedStatement ps = mConnection.prepareStatement(sql);
        ps.setInt(1, projectId);
        ResultSet rs = ps.executeQuery();

        // append each result task to the tasks string
        ArrayList<Task> tasks = new ArrayList<>();
        while (rs.next()) {
            // title;start;end;user;ip;port;status
            String title = rs.getString(TITLE_ID);
            Calendar start = CalHelper.getCalendar(rs.getString(START_ID));
            Calendar end = CalHelper.getCalendar(rs.getString(END_ID));
            String user = rs.getString(USER_ID);
            String ip = rs.getString(IP_ID);
            int port = rs.getInt(PORT_ID);
            boolean status = rs.getBoolean(STATUS_ID);

            tasks.add(new Task(title, start, end, user, ip, port, status));
        }
        ps.close();
        rs.close();
        return tasks;
    }

    public Events.ProjectOK assignUser(Events.Take inObj, String ip, int port) throws SQLException {
        String sql = "UPDATE " +
                TASKS_TABLE + " " +
                "SET " +
                USER_ID + " = ?, " +
                IP_ID + " = ?, " +
                PORT_ID + " = ?, " +
                STATUS_ID + " = ? " +
                "WHERE " +
                PROJECT_ID + " = (SELECT " + PROJECT_ID + " FROM " + PROJECTS_TABLE + " WHERE " + TITLE_ID + " = ?) AND " +
                TITLE_ID + " = ?";

        // sql escape using PreparedStatement... prevent sql injection
        PreparedStatement ps = mConnection.prepareStatement(sql);
        ps.setString(1, inObj.getmUser());
        ps.setString(2, ip);
        ps.setInt(3, port);
        ps.setString(4, Tags.WAITING_TAG);
        ps.setString(5, inObj.getmProject());
        ps.setString(6, inObj.getmTask());

        int rows = ps.executeUpdate();
        ps.close();

        int status = (rows > 0) ? 0 : -1;
        return new Events.ProjectOK(status);
    }

    /**
     * Gets all the Projects saved in the database. The information returned only contains
     * the titles of each Project.
     *
     * @return - string containing all the project titles separated by the ';' character
     * @throws SQLException - raises exception if the SQL command was not able to execute
     */
    public Events.ProjectsAnswer getProjects() throws SQLException, ParseException {

        // select all from the projects table
        String sql = "SELECT * FROM " + PROJECTS_TABLE;

        // sql escape using PreparedStatement. prevent sql injection
        PreparedStatement ps = mConnection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        // append the exam titles to a string, and get the count
        String exams = "";
        ArrayList<Project> projects = new ArrayList<>();
        while (rs.next()) {
            int projectId = rs.getInt(PROJECT_ID);
            projects.add(new Project(rs.getString(TITLE_ID), getProjectTasks(projectId)));
        }

        // example output format: PROJECTS:2;Exam;Enigma
        ps.close();
        rs.close();
        return new Events.ProjectsAnswer(projects);
    }
}
