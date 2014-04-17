package com.nearform.quartz;

public class JobData {

    private String firstName;
    private String lastName;

    private boolean isVerified;

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    public void setFirstName(String s) { firstName = s; }
    public void setLastName(String s) { lastName = s; }

    public boolean isVerified() { return isVerified; }

    public void setVerified(boolean b) { isVerified = b; }
}
