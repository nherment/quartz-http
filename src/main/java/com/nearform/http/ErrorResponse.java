package com.nearform.http;

import java.io.StringWriter;
import java.io.PrintWriter;

public class ErrorResponse {

  public ErrorResponse(Exception e) {
    exception = e;
  }

  private Exception exception;

//  private String message;
//  private String stacktrace;
//  private String group;
//  private String payload;
//  private int timestamp;

  public String getMessage() { return exception.getMessage(); }
  public String getStackTrace() {
    StringWriter sw = new StringWriter();
    exception.printStackTrace(new PrintWriter(sw));
    String exceptionAsString = sw.toString();
    return exceptionAsString;
  }

}
