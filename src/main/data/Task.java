package main.data;

import main.ASN1.ASN1DecoderFail;
import main.ASN1.ASNObj;
import main.ASN1.Decoder;
import main.ASN1.Encoder;

import java.util.Arrays;
import java.util.Calendar;


public class Task extends ASNObj {
    private String name;
    private Calendar start;
    private Calendar end;
    private String ip;
    private int port;
    private boolean done;

    public Task() {
    }

    @Override
    public Task instance(){
        return new Task();
    }

    @Override
    public Encoder getEncoder() {
        Encoder enc = new Encoder().initSequence();
        enc.addToSequence(new Encoder(name, Encoder.TAG_UTF8String));
        enc.addToSequence(new Encoder(start));
        enc.addToSequence(new Encoder(end));
        enc.addToSequence(new Encoder(ip, Encoder.TAG_UTF8String));
        enc.addToSequence(new Encoder(port));
        enc.addToSequence(new Encoder(done));
        return enc;
    }

    @Override
    public Task decode(Decoder dec) throws ASN1DecoderFail {
        Decoder content = dec.getContent();
        name = content.getFirstObject(true).getString();
        start = content.getFirstObject(true).getGeneralizedTimeCalender(Encoder.TAG_GeneralizedTime);
        end = content.getFirstObject(true).getGeneralizedTimeCalender(Encoder.TAG_GeneralizedTime);
        ip = content.getFirstObject(true).getString();
        port = content.getFirstObject(true).getInteger().intValue();
        done = content.getFirstObject(true).getBoolean();
        return this;
    }

    public Task(String name, Calendar start, Calendar end, String ip, int port, boolean done) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.ip = ip;
        this.port = port;
        this.done = done;
    }

    @Override
    public String toString() {
        return String.format("name=%s, start=%s, end=%s, ip=%s, port=%d, done=%s", name, start.toString(), end.toString(), ip, port, done);
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
