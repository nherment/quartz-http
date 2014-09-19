package com.nearform.quartz;

// The jobId should be of the form group::name
// This class allows the serialization of the jobId field from a JSON object
// and the subsequent extraction of the name and/or group from the jobId
public class JobDataId {
	
	// jobId holds the Group::TriggerName/JobName
	// Group is "http"
	// TriggerName and JobName's are UUIDs
	// So a jobId will look something like:
	// http::eabcb5ef-f305-4fbb-b961-39525cbc7ae6+effd055f-a5f2-4841-a7fb-d098efbb7372
	  private String jobId;

	  public String getJobId() { return jobId; }
	  public void setJobId(String s) { jobId = s; }
	  
	  public String getTriggerName() 
	  {
		  return getName().split(triggerJobDelimiter)[0];
	  }
	  public String getJobName() 
	  {
		  String[] parts = getName().split(triggerJobDelimiter);
		  if(parts.length > 1)
		  {
			  return parts[1];
		  }
		  return "";
	  }
	  private String getName() 
	  { 
		  if(jobId == null)
		  {
			  return "";
		  }
		  String[] parts = jobId.split(groupDelimiter);
		  if(parts.length > 1) 
		  {
			  return parts[1];
		  }
		  return "";
	  }
	  public String getGroup() 
	  { 
		  if(jobId == null)
		  {
			  return "";
		  }
		  return jobId.split(groupDelimiter)[0];
	  }
	  
	  public static String groupDelimiter = "::";
	  public static String triggerJobDelimiter = "_";

}
