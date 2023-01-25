package com.ag.logviewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ag.logviewer.restservice.controller.LogViewController;

@SpringBootApplication
public class LogViewerApplication {

  private static final Logger logger = LoggerFactory.getLogger(LogViewController.class);

  public static void main(String[] args) {
    logger.info("*** Server is up and ready to serve the logs. Specify a file in /var/logs to get started. ***");
    SpringApplication.run(LogViewerApplication.class, args);
  }

}
