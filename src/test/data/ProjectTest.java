package test.data;

import main.ASN1.ASN1DecoderFail;
import main.ASN1.Decoder;
import main.ASN1.Encoder;
import main.data.Project;
import main.data.Task;
import main.utils.CalHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;

public class ProjectTest {

    private Project mProject;
    private String projectName = "Project Name";
    private String taskPrefix = "task";
    private String ip = "192.168.1.103";
    private Calendar date;
    ArrayList<Task> mTasks = new ArrayList<>();
    private int port = 4232;
    private boolean done = false;

    @Before
    public void setUp() throws Exception {
        date = CalHelper.getCalendar("2016-03-18:18h30m00s001Z");
        for (int i = 0; i < 10; i++) {
            mTasks.add(new Task(taskPrefix + i, date, date, null, ip, port, done));
        }
        mProject = new Project(projectName, mTasks);
    }

    @Test
    public void testEncodesCorrectly() throws ASN1DecoderFail {
        Encoder enc = mProject.getEncoder();
        assert enc.getBytes().length == 752;
    }

    @Test
    public void testDecoderMethod() throws Exception {
        Decoder dec = new Decoder(mProject.getEncoder().getBytes());
        dec = dec.getContent();

        assert projectName.equals(dec.getFirstObject(true).getString());
        ArrayList<Task> tasks = dec.getFirstObject(true).getSequenceOfAL(Encoder.TAG_SEQUENCE, new Task());
        assert tasks.size() == mTasks.size();
        for (int i = 0; i < tasks.size(); i++) {
            assert tasks.get(i).equals(mTasks.get(i));
        }
    }
}