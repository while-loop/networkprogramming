package main.data;

import main.ASN1.ASN1DecoderFail;
import main.ASN1.ASNObj;
import main.ASN1.Decoder;
import main.ASN1.Encoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Events {

    public static class Take extends ASNObj {
        private String mUser;
        private String mProject;
        private String mTask;

        public String getmUser() {
            return mUser;
        }

        public String getmProject() {
            return mProject;
        }

        public String getmTask() {
            return mTask;
        }

        public Take() {

        }

        public Take(String rawInput) {
            String editedInput = rawInput.replace(Tags.TAKE_TAG + ";", "");

            //USER:Johny;PROJECT:Exam;Buy paper
            // regex: match the user, project title,  and task title. up until a \r or \n
            Pattern verbPattern = Pattern.compile(Tags.USER_TAG + ":(.*);" + Tags.PROJECT_TAG + ":(.*);(.*)[^\r^\n]*");

            Matcher m = verbPattern.matcher(editedInput);
            if (!m.find()) {
                System.out.println("No regex matches for TAKE.");
            } else {
                mUser = m.group(1);      // user
                mProject = m.group(2);   // project_title
                mTask = m.group(3);      // task_title
            }
        }

        @Override
        public Encoder getEncoder() {
            Encoder enc = new Encoder().initSequence();
            enc.addToSequence(new Encoder(mUser, Encoder.TAG_UTF8String));
            enc.addToSequence(new Encoder(mProject, Encoder.TAG_UTF8String));
            enc.addToSequence(new Encoder(mTask, Encoder.TAG_UTF8String));
            return enc.setASN1Type(Tags.TAG_AC5);
        }

        @Override
        public Take decode(Decoder dec) throws ASN1DecoderFail {
            Decoder content = dec.getContent();
            mUser = content.getFirstObject(true).getString();
            mProject = content.getFirstObject(true).getString();
            mTask = content.getFirstObject(true).getString();
            return this;
        }

        public Take(String user, String project, String task) {
            mUser = user;
            mProject = project;
            mTask = task;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("mUser=").append(mUser).append(" ");
            sb.append("mProject=").append(mProject).append(" ");
            sb.append("mTask=").append(mTask).append(" ");
            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Take)) {
                return false;
            }
            Take other = (Take) obj;
            return Arrays.equals(this.getEncoder().getBytes(), other.getEncoder().getBytes());
        }
    }

    public static class ProjectOK extends ASNObj {
        private int mCode;
        private Project mProject; // OPTIONAL

        public ProjectOK() {

        }

        public ProjectOK(int code) {
            mCode = code;
        }

        @Override
        public Encoder getEncoder() {
            Encoder enc = new Encoder().initSequence();
            enc.addToSequence(new Encoder(mCode));
            if (mProject != null) {
                enc.addToSequence(mProject.getEncoder()).setASN1Type(Tags.TAG_AC0);
            }
            return enc.setASN1Type(Tags.TAG_AC0);
        }

        @Override
        public ProjectOK decode(Decoder dec) throws ASN1DecoderFail {
            Decoder content = dec.getContent();
            mCode = content.getFirstObject(true).getInteger().intValue();
            if (content.getTypeByte() == Tags.TAG_AC1) {
                mProject = new Project().decode(content.getFirstObject(true));
            }
            return this;
        }

        public ProjectOK(int code, Project project) {
            mCode = code;
            mProject = project;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (mCode == 0) {
                sb.append(Tags.OK_TAG);
            } else {
                sb.append(Tags.FAIL_TAG);
            }
            if (mProject != null) sb.append(";").append(mProject.toString());
            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof ProjectOK)) {
                return false;
            }
            ProjectOK other = (ProjectOK) obj;
            return Arrays.equals(this.getEncoder().getBytes(), other.getEncoder().getBytes());
        }
    }

    public static class ProjectsAnswer extends ASNObj {
        private ArrayList<Project> mProjects;

        public ProjectsAnswer(String rawInput) {

        }

        public ProjectsAnswer() {

        }

        @Override
        public Encoder getEncoder() {
            Encoder enc = new Encoder().initSequence();
            enc.addToSequence(Encoder.getEncoder(mProjects));
            return enc.setASN1Type(Tags.TAG_AC3);
        }

        @Override
        public ProjectsAnswer decode(Decoder dec) throws ASN1DecoderFail {
            Decoder content = dec.getContent();
            mProjects = content.getFirstObject(true).getSequenceOfAL(Tags.TAG_AC1, new Project());
            return this;
        }

        public ProjectsAnswer(ArrayList<Project> projects) {
            mProjects = projects;
        }

        @Override
        public String toString() {
            // OK;PROJECTS:2;Exam;Enigma
            StringBuilder sb = new StringBuilder();
            sb.append(Tags.OK_TAG).append(";");
            sb.append(Tags.PROJECTS_TAG).append(":").append(mProjects.size());
            for (Project proj : mProjects) {
                sb.append(";").append(proj.getmName());
            }
            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof ProjectsAnswer)) {
                return false;
            }
            ProjectsAnswer other = (ProjectsAnswer) obj;
            return Arrays.equals(this.getEncoder().getBytes(), other.getEncoder().getBytes());
        }
    }

    public static class GetProject extends ASNObj {
        private String mName;

        public String getmName() {
            return mName;
        }

        public GetProject() {

        }

        @Override
        public Encoder getEncoder() {
            Encoder enc = new Encoder().initSequence();
            enc.addToSequence(new Encoder(mName, Encoder.TAG_UTF8String));
            return enc.setASN1Type(Tags.TAG_AC4);
        }

        @Override
        public GetProject decode(Decoder dec) throws ASN1DecoderFail {
            Decoder content = dec.getContent();
            mName = content.getFirstObject(true).getString();
            return this;
        }

        public GetProject(String rawInput) {
            //"GET_PROJECT;Exam"
            String editedInput = rawInput.replace(Tags.GET_PROJECT_TAG + ";", "");
            Pattern verbPattern = Pattern.compile("([^\r^\n]*)"); // match all characters up to a \r or \n

            Matcher m = verbPattern.matcher(editedInput);
            if (!m.find()) {
                System.out.println("No match for GET_PROJECT.");
            } else {
                mName = m.group(1);
            }

        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("mName=").append(mName).append(" ");
            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof GetProject)) {
                return false;
            }
            GetProject other = (GetProject) obj;
            return Arrays.equals(this.getEncoder().getBytes(), other.getEncoder().getBytes());
        }
    }

    public static class Projects extends ASNObj {

        public Projects() {
        }

        @Override
        public Encoder getEncoder() {
            return new Encoder().initSequence().setASN1Type(Tags.TAG_AC2);
        }

        @Override
        public Projects decode(Decoder dec) throws ASN1DecoderFail {
            return this;
        }

        @Override
        public String toString() {
            return "GetProjects=";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Projects)) {
                return false;
            }
            Projects other = (Projects) obj;
            return Arrays.equals(this.getEncoder().getBytes(), other.getEncoder().getBytes());
        }
    }

}

