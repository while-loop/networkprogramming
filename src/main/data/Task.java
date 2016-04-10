package main.data;

import main.ASN1.ASN1DecoderFail;
import main.ASN1.ASNObj;
import main.ASN1.Decoder;
import main.ASN1.Encoder;
import main.utils.CalHelper;

import java.util.Arrays;
import java.util.Calendar;


public class Task extends ASNObj {
    private String name;
    private String user;
    private Calendar start;
    private Calendar end;
    private String ip;      // OPTIONAL
    private int port;       // OPTIONAL
    private boolean done;   // OPTIONAL

    public Task() {
    }

    public String getName() {
        return name;
    }

    public String getUser() {
        return user;
    }

    public Calendar getStart() {
        return start;
    }

    public Calendar getEnd() {
        return end;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean isDone() {
        return done;
    }

    @Override
    public Task instance() {
        return new Task();
    }

    @Override
    public Encoder getEncoder() {
        Encoder enc = new Encoder().initSequence();
        enc.addToSequence(new Encoder(name, Encoder.TAG_UTF8String));
        enc.addToSequence(new Encoder(start));
        enc.addToSequence(new Encoder(end));
        if (user != null) enc.addToSequence(new Encoder(user, Encoder.TAG_UTF8String));
        if (ip != null) enc.addToSequence(new Encoder(ip, Encoder.TAG_UTF8String));
        if (port != 0) enc.addToSequence(new Encoder(port));
        enc.addToSequence(new Encoder(done));
        return enc.setASN1Type(Tags.TAG_AC1);
    }

    @Override
    public Task decode(Decoder dec) throws ASN1DecoderFail {
        Decoder content = dec.getContent();
        if (content.getTypeByte() == Encoder.TAG_UTF8String) name = content.getFirstObject(true).getString();
        if (content.getTypeByte() == Encoder.TAG_GeneralizedTime)
            start = content.getFirstObject(true).getGeneralizedTimeCalender(Encoder.TAG_GeneralizedTime);
        if (content.getTypeByte() == Encoder.TAG_GeneralizedTime)
            end = content.getFirstObject(true).getGeneralizedTimeCalender(Encoder.TAG_GeneralizedTime);
        if (content.getTypeByte() == Encoder.TAG_UTF8String) user = content.getFirstObject(true).getString();
        if (content.getTypeByte() == Encoder.TAG_UTF8String) ip = content.getFirstObject(true).getString();
        if (content.getTypeByte() == Encoder.TAG_INTEGER) port = content.getFirstObject(true).getInteger().intValue();
        if (content.getTypeByte() == Encoder.TAG_BOOLEAN) done = content.getFirstObject(true).getBoolean();
        return this;
    }

    public Task(String name, Calendar start, Calendar end, String user, String ip, int port, boolean done) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.user = user;
        this.ip = ip;
        this.port = port;
        this.done = done;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(";");
        sb.append(CalHelper.getStringDate(start)).append(";");
        sb.append(CalHelper.getStringDate(end)).append(";");
        if (user != null) sb.append(user).append(";");
        if (ip != null) sb.append(ip).append(";");
        if (port != 0) sb.append(port).append(";");
        if (done) {
            sb.append(Tags.DONE_TAG);
        } else {
            sb.append(Tags.WAITING_TAG);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Task)) {
            return false;
        }
        Task other = (Task) obj;
        return Arrays.equals(this.getEncoder().getBytes(), other.getEncoder().getBytes());
    }
}
