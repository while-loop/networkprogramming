package test.data;

import main.ASN1.ASN1DecoderFail;
import main.ASN1.Decoder;
import main.ASN1.Encoder;
import main.data.Task;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;

public class TaskTest {

    private String name = "Test Task";
    private Calendar start;
    private Calendar end;
    private String ip = "localhost";
    private int port = 4232;
    private boolean done = false;

    String dateString = "2016-03-15:18h30m00s001Z";

    Task mTask;

    @Before
    public void setUp() throws ParseException {
//        start = CalHelper.getCalendar(dateString);
//        end = CalHelper.getCalendar(dateString);
        mTask = new Task(name, null, null, ip, port, done);
    }

    @Test
    public void testEncodesCorrectly() throws ASN1DecoderFail {
        Encoder enc = mTask.getEncoder();
        //assert enc.getBytes().length == 73;
        assert enc.getBytes().length == 35;
    }

    @Test
    public void testDecoderMethod() throws Exception {
        Decoder dec = new Decoder(mTask.getEncoder().getBytes());
        dec = dec.getContent();
        assert name.equals(dec.getFirstObject(true).getString());
        dec.getFirstObject(true);
        dec.getFirstObject(true);
        // assert start.equals(dec.getFirstObject(true).getGeneralizedTimeCalender_());
        //assert end.equals(dec.getFirstObject(true).getGeneralizedTimeCalender_());
        assert ip.equals(dec.getFirstObject(true).getString());
        assert port == dec.getFirstObject(true).getInteger().intValue();
        assert done == dec.getFirstObject(true).getBoolean();
    }

    @Test
    public void testBuildsFromDecode() throws Exception {

    }
}