package com.nearform.quartz;

public class JobData {

  private String url;
  private String name;
  private String group;
  private String payload;
  private long timestamp;

  public String getUrl() { return url; }
  public String getName() { return name; }
  public String getGroup() { return group; }
  public String getPayload() { return payload; }
  public long getTimestamp() { return timestamp; }

  public void setUrl(String s) { url = s; }
  public void setName(String s) { name = s; }
  public void setGroup(String s) { group = s; }
  public void setPayload(String s) { payload = s; }
  public void setTimestamp(long i) { timestamp = i; }
}
