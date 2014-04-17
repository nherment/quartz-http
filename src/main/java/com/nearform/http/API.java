package com.nearform.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.nearform.quartz.JobData;

public class API extends AbstractHandler {

  private ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally

  public void handle(String target,
                     Request baseRequest,
                     HttpServletRequest request,
                     HttpServletResponse response)
                     throws IOException, ServletException {

        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String json = "";
        if(br != null){
          String nextLine = br.readLine();
          while(nextLine != null) {
            json += nextLine;
            nextLine = br.readLine();
          }
          br.close()
        }

        JobData jobData = this.mapper.readValue(json, JobData.class);

        response.setContentType("application/json");

        mapper.writeValue(response.getOutputStream(), jobData);
  }

  public static void main(String[] args) throws Exception {
    Server server = new Server(8080);
    server.setHandler(new API());

    server.start();
    server.join();
  }
}
