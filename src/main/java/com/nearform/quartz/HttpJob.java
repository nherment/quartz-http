package com.nearform.quartz;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import java.io.IOException;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpJob implements Job {

	public void execute(JobExecutionContext context) throws JobExecutionException {

    CloseableHttpClient httpclient = HttpClients.createDefault();
    try {

      JobDataMap dataMap = context.getJobDetail().getJobDataMap();

      String url = dataMap.getString("url");
      String jsonPayload = dataMap.getString("payload");

      System.out.println("Executing job " + context.getJobDetail().getKey().toString() + ", url:" + url + ", payload:" + jsonPayload);

      HttpPost httppost = new HttpPost(url);

      StringEntity reqEntity = new StringEntity(
          jsonPayload,
          "application/json", "UTF-8");

      httppost.setEntity(reqEntity);

      System.out.println("Executing request: " + httppost.getRequestLine());

      CloseableHttpResponse response = httpclient.execute(httppost);
      try {
        System.out.println("http response: " + response.getStatusLine());
        EntityUtils.consume(response.getEntity());
      } finally {
        response.close();
      }
    } catch(IOException ioe) {
      ioe.printStackTrace();
    } finally {
      try {
        httpclient.close();
      } catch(IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

}
