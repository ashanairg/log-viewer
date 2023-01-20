package com.ag.logviewer.restservice.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ag.logviewer.restservice.service.LogViewService;

@RestController
public class LogViewController {
  private static final Logger logger = LoggerFactory.getLogger(LogViewController.class);
  
  private final LogViewService logViewService;
  
  @Autowired
  public LogViewController(LogViewService logViewService) {
    this.logViewService = logViewService;
  }
 
  @GetMapping("/log")
  public List<String> greetings(@RequestParam(required = false, value = "file") String fileName) {
    List<String> logs = new ArrayList<>();
    logs.add("Welcome to log view");
    logs.add("Lets get started");
    return logs;
  }

  @GetMapping("/logs")
  public List<String> viewLogs(@RequestParam(value = "file") String fileName)
      throws IOException {
    String path = "/var/log/" + fileName;
    logger.info("Request received to view logs for the file {}.", path); 
    
    // Validity checks
    File file = new File(path);
    boolean validRequest = isValidRequest(file);
    if (!validRequest) {
      // return httpstatus error
    }
       
    
    logger.info("All validations passed. Starting to fetch logs from file {}.", fileName);
    return logViewService.getLogs(file);
  }

  private boolean isValidRequest(File file) throws IOException {
    // Is it a valid file
    // Is is binary
    // Is it directory
    boolean validRequest = true;
    // Logging the errors for now. Not throwing an exception.
    // Can be added if needed for NR metrics.
    if (!file.exists()) {
      validRequest = false;
      logger.error("The requested log file {} does not exist.", file.getAbsolutePath());
    } else if (!file.isDirectory()) {
      validRequest = false;
      logger.error("The requested log file {} is a directory.", file.getAbsolutePath());
    } else if (isBinaryFile(file)) {
      validRequest = false;
      logger.error("The requested log file {} is not a text file.", file.getAbsolutePath());
    }
    
    return validRequest;
    
  }

  // A somewhat simplistic way of finding the mime type based on extenstions alone
  // Probably can be refined to check the file contents as well
  private boolean isBinaryFile(File file) throws IOException {
    String type = Files.probeContentType(file.toPath());
    if (type.startsWith("text")) {
        return false;
    }

    // type couldn't be determined or is binary
    return true;
  }

}
