package main.data;

import main.db.MyDB;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    static MyDB myDB;
    String clientIP;
    int clientPort;

    public Parser(MyDB db) throws IOException, SQLException {
        if (myDB == null) {
            myDB = db;
            myDB.connect();
        }

    }

    public void close(){
        if (myDB != null) {
            myDB.disconnect();
        }
    }

    public String processInput(String input) {
        return processInput(input, this.clientIP, this.clientPort);
    }

        /**
         * Parse and process a command given by the client.
         * Currently supports 4 commands:
         * PROJECT_DEFINITION
         * TAKE
         * GET_PROJECTS
         * GET_PROJECT
         *
         * @param input - raw command input given by the client
         * @return - string to return back to the client depending on the command given
         */
    public String processInput(String input, String ip, int port) {
        String status = Tags.OK_TAG; // status tag to send back to the client. OK/FAIL
        String verb = getVerb(input);
        if (verb.equals(Tags.TAKE_TAG)) { // assign user to a task
            try {
                String[] info = getUserInfo(input);
                int rows = myDB.assignUser(info[0], info[1], info[2], ip, port);
                if (rows < 1)
                    status = Tags.FAIL_TAG;
            } catch (SQLException e) {
                status = Tags.FAIL_TAG;
            }
        } else if (verb.equals(Tags.GET_PROJECT_TAG)) { // all information about a task
            try {
                input = myDB.getProject(getTitle(input));
            } catch (SQLException e) {
                status = Tags.FAIL_TAG;
            }
        } else if (verb.equals(Tags.GET_PROJECTS_TAG)) { // get count of projects and their names
            try {
                input = myDB.getProjects();
            } catch (SQLException e) {
                System.err.println(e.toString());
                status = Tags.FAIL_TAG;
            }
        } else if (verb.equals(Tags.PROJECT_DEFINITION_TAG)) { // add a project to the database
            try {
                myDB.addProject(input);
            } catch (Exception e) {
                System.err.println("Unable to add project.");
                System.err.println(e.toString());
                status = Tags.FAIL_TAG;
            }
        } else {
            status = Tags.FAIL_TAG;
        }

        return (status + ";" + input);
    }

    /**
     * Parse user information when adding a user to a specific task.
     *
     * @param input - raw client command containing the User information
     * @return - String array of length 3 containing information to update the database.
     * array[0] - User Name
     * array[1] - Project name to assign the user to
     * array[2] - Task name to assign the user to
     */
    private String[] getUserInfo(String input) {
        String editedInput = input.replace(Tags.TAKE_TAG + ";", "");
        String[] temp = new String[3];

        //USER:Johny;PROJECT:Exam;Buy paper
        // regex: match the user, project title,  and task title. up until a \r or \n
        Pattern verbPattern = Pattern.compile(Tags.USER_TAG + ":(.*);" + Tags.PROJECT_TAG + ":(.*);(.*)[^\r^\n]*");

        Matcher m = verbPattern.matcher(editedInput);
        if (!m.find()) {
            System.out.println("No match.");
            return temp;
        }

        temp[0] = m.group(1);   // user
        temp[1] = m.group(2);   // project_title
        temp[2] = m.group(3);   // task_title
        return temp;
    }

    /**
     * Gets the title of a Project from the client's command
     *
     * @param input - raw input of the client
     * @return - Title of the project
     */
    private String getTitle(String input) {
        String editedInput = input.replace(Tags.GET_PROJECT_TAG + ";", "");
        Pattern verbPattern = Pattern.compile("([^\r^\n]*)"); // match all characters up to a \r or \n

        Matcher m = verbPattern.matcher(editedInput);
        if (!m.find()) {
            System.out.println("No match.");
            return "";
        }

        return m.group(1);
    }

    /**
     * Get the verb of the command given by the client.
     * Currently the working verbs are:
     * PROJECT_DEFINITION
     * TAKE
     * GET_PROJECTS
     * GET_PROJECT
     *
     * @param input - raw input from the client
     * @return - the verb
     */
    private String getVerb(String input) {
        Pattern verbPattern = Pattern.compile("([^;^:^\r^\n]*)");

        Matcher m = verbPattern.matcher(input);
        if (!m.find()) {
            System.out.println("No match.");
            return "";
        }

        return m.group(1);
    }

    public void setClientInfo(String hostAddress, int localPort) {
        this.clientIP = hostAddress;
        this.clientPort = localPort;
    }
}
