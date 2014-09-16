package com.nearform.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.util.UUID;
import java.util.Date;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;



//import javax.servlet.*;
import javax.servlet.http.*;

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

public class API extends HttpServlet {

	private static final long serialVersionUID = 7749936643683585485L;
	private ObjectMapper mapper = new ObjectMapper(); // can reuse, share
														// globally
	private Scheduler scheduler;

	public void init() throws ServletException {
		try {
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

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

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
			mapper.writeValue(response.getOutputStream(), new ErrorResponse(e));
		}
	}

	public void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

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

		ScheduleResponse jobData = this.mapper.readValue(json, ScheduleResponse.class);

		response.setContentType("application/json");

		ScheduleResponse responseContent = unschedule(jobData);
		mapper.writeValue(response.getOutputStream(), responseContent);
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
		response.setKey(triggerKey.getGroup() + "::" + triggerKey.getName());

		System.out.println("Scheduling job " + startTime);

		return response;
	}

	// Delete the Job and Unschedule All of Its Triggers
	private ScheduleResponse unschedule(ScheduleResponse jobDataCancel) {
		String[] parts = jobDataCancel.getKey().split("::");
		ScheduleResponse response = new ScheduleResponse();
		response.setKey("false");
		if(parts.length == 2) {
			try {
				// JobKey takes a name as first param and group as second param
				// We've stored it the other way around so reference the array "backwards"
				scheduler.deleteJob(new JobKey(parts[1], parts[0]));
				response.setKey("true");
				return response;
			} catch (SchedulerException e) {
				System.out.println("Failed to delete job");
				e.printStackTrace();
			}
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
