package com.nearform.quartz;

// The jobId should be of the form group::name
// This class allows the serialization of the jobId field from a JSON object
// and the subsequent extraction of the name and/or group from the jobId
public class JobDataId {
	  private String jobId;

	  public String getJobId() { return jobId; }
	  public void setJobId(String s) { jobId = s; }
	  
	  public String getName() 
	  { 
		  if(jobId == null)
		  {
			  return "";
		  }
		  String[] parts = jobId.split("::");
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
		  return jobId.split("::")[0];
	  }

}
