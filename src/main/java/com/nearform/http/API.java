package com.nearform.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.util.UUID;
import java.util.Date;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

// Server/main is not needed because Jetty will run this for us.
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.server.Request;
//import org.eclipse.jetty.server.handler.AbstractHandler;



//import javax.servlet.*;
import javax.servlet.http.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;

import com.nearform.quartz.JobData;
import com.nearform.quartz.HttpJob;
import com.nearform.quartz.JobDataId;

public class API extends HttpServlet {

	private static final long serialVersionUID = 7749936643683585485L;
	private ObjectMapper mapper = new ObjectMapper(); // can reuse, share
														// globally
	private Scheduler scheduler;

	public void init() throws ServletException {
		try {
			// The next line prevents failure when deserializing a JSON object that has
			// more properties than we've defined in the class that we're deserializing
			// the data into.
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			throw new ServletException(
					"Could not start due to scheduler exception", e);
		}
	}

	public void destroy() {
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	// This is the C from Crud. Create a new job in the scheduler
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		System.out.println("POST: Creating a job start.");

		BufferedReader br = new BufferedReader(new InputStreamReader(
				request.getInputStream()));
		String json = "";
		if (br != null) {
			String nextLine = br.readLine();
			while (nextLine != null) {
				json += nextLine;
				nextLine = br.readLine();
			}
			br.close();
		}
		System.out.println(json);

		JobData jobData = this.mapper.readValue(json, JobData.class);

		response.setContentType("application/json");

		try {
			ScheduleResponse responseContent = schedule(jobData);
			mapper.writeValue(response.getOutputStream(), responseContent);
		} catch (SchedulerException e) {
			System.out.println("ERROR: " + e);
			e.printStackTrace();
			mapper.writeValue(response.getOutputStream(), new ErrorResponse(e));
		}
		System.out.println("POST: Creating a job end.");
	}

	// This is the U from crUd. Update an existing job in the scheduler
	public void doPut(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		System.out.println("PUT: Updating a job start.");

		BufferedReader br = new BufferedReader(new InputStreamReader(
				request.getInputStream()));
		String json = "";
		if (br != null) {
			String nextLine = br.readLine();
			while (nextLine != null) {
				json += nextLine;
				nextLine = br.readLine();
			}
			br.close();
		}
		System.out.println(json);

		JobData jobData = this.mapper.readValue(json, JobData.class);
		JobDataId jobDataId = this.mapper.readValue(json, JobDataId.class);

		response.setContentType("application/json");

		try {
			ScheduleResponse responseContent = update(jobData, jobDataId);
			mapper.writeValue(response.getOutputStream(), responseContent);
		} catch (SchedulerException e) {
			System.out.println("ERROR: " + e);
			e.printStackTrace();
			mapper.writeValue(response.getOutputStream(), new ErrorResponse(e));
		}
		System.out.println("PUT: Updating a job end.");
	}

	// This is the D from cruD. Delete a job from the scheduler
	public void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		System.out.println("DELETE: Canceling a job start.");

		String path = request.getRequestURI();
		String[] parts = path.split("/");
		String key = parts[parts.length-1];
		
		System.out.println("key to delete: " + key);
		
		JobDataId jobDataId = new JobDataId();
		jobDataId.setJobId(key);

		response.setContentType("application/json");

		ScheduleResponse responseContent = unschedule(jobDataId);
		mapper.writeValue(response.getOutputStream(), responseContent);
		System.out.println("DELETE: Canceling a job end.");
	}

	private ScheduleResponse update(JobData jobData, JobDataId jobDataId)
			throws SchedulerException {
		System.out.println(jobData);

		// Add the new job to the scheduler, instructing it to "replace"
		//  the existing job with the given name and group (if any)
		JobDetail job = newJob(HttpJob.class)
			    .withIdentity(jobDataId.getJobName(), jobDataId.getGroup())
				.usingJobData("url", jobData.getUrl())
				.usingJobData("payload", jobData.getPayload()).build();

		// addJob(JobDetail jobDetail, boolean replace, boolean storeNonDurableWhileAwaitingScheduling)
        // Add the given Job to the Scheduler - with no associated Trigger.     
		scheduler.addJob(job, true, true);

		// Now adjust the trigger to the new time.
		Date startTime = new Date(jobData.getTimestamp());

		Trigger oldTrigger = scheduler.getTrigger(TriggerKey.triggerKey(jobDataId.getTriggerName(), jobDataId.getGroup()));
		
		Trigger newTrigger = newTrigger()
				.withIdentity(jobDataId.getTriggerName(), jobDataId.getGroup())
				.startAt(startTime).build();
		
		scheduler.rescheduleJob(oldTrigger.getKey(), newTrigger);

		ScheduleResponse response = new ScheduleResponse();
		response.setKey(jobDataId.getJobId());

		System.out.println("Upading job with key: " + jobDataId.getJobId());

		return response;
	}

	private ScheduleResponse schedule(JobData jobData)
			throws SchedulerException {

		JobDetail job = newJob(HttpJob.class)
				.withIdentity(UUID.randomUUID().toString(), "http")
				.usingJobData("url", jobData.getUrl())
				.usingJobData("payload", jobData.getPayload()).build();

		Date startTime = new Date(jobData.getTimestamp());

		Trigger trigger = newTrigger()
				.withIdentity(UUID.randomUUID().toString(), "http")
				.startAt(startTime).build();

		scheduler.scheduleJob(job, trigger);

		TriggerKey triggerKey = trigger.getKey();
		ScheduleResponse response = new ScheduleResponse();
		response.setKey(triggerKey.getGroup() + JobDataId.groupDelimiter + triggerKey.getName() + JobDataId.triggerJobDelimiter + job.getKey().getName());

		System.out.println("Scheduling job " + startTime);

		return response;
	}

	// Delete the Job and Unschedule All of Its Triggers
	private ScheduleResponse unschedule(JobDataId jobDataId) {
		ScheduleResponse response = new ScheduleResponse();
		response.setKey("false");
		try {
			// JobKey takes a name as first param and group as second param
			// We've stored it the other way around so reference the array "backwards"
			scheduler.deleteJob(new JobKey(jobDataId.getJobName(), jobDataId.getGroup()));
			response.setKey("true");
			System.out.println("Canceling job: " + jobDataId.getJobName());
			return response;
		} catch (SchedulerException e) {
			System.out.println("Failed to cancel job: " + jobDataId.getJobName());
			e.printStackTrace();
		}
		return response;
	}

/*	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);

		server.setHandler(new API());

		server.start();
		server.join();
	}*/
}
