package main.data;

import main.ASN1.ASN1DecoderFail;
import main.ASN1.ASNObj;
import main.ASN1.Decoder;
import main.ASN1.Encoder;

import java.util.ArrayList;
import java.util.Arrays;

public class Project extends ASNObj {
    private String mName;
    private ArrayList<Task> mTasks = new ArrayList<>();

    @Override
    public Encoder getEncoder() {
        Encoder enc = new Encoder().initSequence();
        enc.addToSequence(new Encoder(mName, Encoder.TAG_UTF8String));
        enc.addToSequence(Encoder.getEncoder(mTasks));
        return enc;
    }

    @Override
    public Project decode(Decoder dec) throws ASN1DecoderFail {
        Decoder content = dec.getContent();
        mName = content.getFirstObject(true).getString();
        mTasks = content.getFirstObject(true).getSequenceOfAL(Encoder.TAG_SEQUENCE, new Task());
        return this;
    }

    public Project(String name, ArrayList<Task> tasks) {
        mName = name;
        mTasks = tasks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mName=").append(mName).append(" ");
        for (Task task : mTasks) {
            sb.append(task.toString()).append(" ");
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
