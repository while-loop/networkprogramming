package main.data;

import main.ASN1.ASN1DecoderFail;
import main.ASN1.ASNObj;
import main.ASN1.Decoder;
import main.ASN1.Encoder;
import main.utils.CalHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Project extends ASNObj {
    private String mName;
    private ArrayList<Task> mTasks = new ArrayList<>();

    public Project() {

    }

    public ArrayList<Task> getmTasks() {
        return mTasks;
    }

    public String getmName() {
        return mName;
    }

    public Project(String rawInput) throws ParseException {
        // "PROJECT_DEFINITION:Exam;TASKS:2;Buy paper;2016-03-12:18h30m00s001Z;2016-03-15:18h30m00s001Z;Write exam;2016-03-15:18h30m00s001Z;2016-03-15:18h30m00s001Z;"
        Pattern projectPattern = Pattern.compile(Tags.PROJECT_DEFINITION_TAG + ":(.*);" + Tags.TASKS_TAG + ":(\\d*);(.*;.*;.*;)*");

        // Now create matcher object and check if we were able to parse the input
        Matcher m = projectPattern.matcher(rawInput);
        if (!m.find()) {
            System.out.println("No regex matches for " + Tags.PROJECT_DEFINITION_TAG);
            throw new ParseException("Unable to parse input java.data.", 0);
        }

        mName = m.group(1);
        int taskSize = Integer.parseInt(m.group(2));
        String[] tasksArray = m.group(3).split(";");
        int taskEntryLength = 3;

        for (int i = 0; i < taskSize; i++) {
            String title = tasksArray[taskEntryLength * i + 0];
            Calendar start = CalHelper.getCalendar(tasksArray[taskEntryLength * i + 1]);
            Calendar end = CalHelper.getCalendar(tasksArray[taskEntryLength * i + 2]);
            mTasks.add(new Task(title, start, end, null, null, 0, false));
        }
    }

    @Override
    public Encoder getEncoder() {
        Encoder enc = new Encoder().initSequence();
        enc.addToSequence(new Encoder(mName, Encoder.TAG_UTF8String));
        enc.addToSequence(Encoder.getEncoder(mTasks).setASN1Type(Tags.TAG_AC1));
        return enc.setASN1Type(Tags.TAG_AC1);
    }

    @Override
    public Project instance() {
        return new Project();
    }

    @Override
    public Project decode(Decoder dec) throws ASN1DecoderFail {
        Decoder content = dec.getContent();
        mName = content.getFirstObject(true).getString();
        mTasks = content.getFirstObject(true).getSequenceOfAL(Tags.TAG_AC1, new Task());
        return this;
    }

    public Project(String name, ArrayList<Task> tasks) {
        mName = name;
        mTasks = tasks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Tags.PROJECT_DEFINITION_TAG).append(":").append(mName).append(";");
        sb.append(Tags.TASKS_TAG).append(":").append(mTasks.size());
        for (Task task : mTasks) {
            sb.append(";").append(task.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Project)) {
            return false;
        }
        Project other = (Project) obj;
        return Arrays.equals(this.getEncoder().getBytes(), other.getEncoder().getBytes());
    }
}
