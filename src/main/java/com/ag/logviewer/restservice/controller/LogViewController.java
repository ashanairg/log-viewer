package com.ag.logviewer.restservice.controller;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    logs.add("Welcome to log view.");
    logs.add("Please specify a file name in /var/logs to view the file.");
    return logs;
  }

  @GetMapping("/logs")
  public ResponseEntity<List<String>> viewLogs(@RequestParam(value = "file") String fileName)
      throws IOException {
    String path = "/var/log/" + fileName;
    logger.info("Request received to view logs for the file {}.", path); 
    
    // Validity checks
    File file = new File(path);
    String errorMessage = isValidLogRequest(file);
    // Should be a custom error class in the future
    if (errorMessage != null) {
      logger.error(errorMessage);
      return ResponseEntity.badRequest().body(Arrays.asList(errorMessage));
    }
       
    logger.info("All validations passed. Starting to fetch logs from file {}.", fileName);
    List<String> logs = logViewService.getLogs(file);
    return ResponseEntity.ok().body(logs);
  }

  @GetMapping("/logs/{file}/search")
  public ResponseEntity<List<String>> searchLogs(
      @PathVariable("file") String fileName,
      @RequestParam("q") String queryString,
      @RequestParam(required = false, value = "last") String lastN)
      throws IOException {
    String path = "/var/log/" + fileName; 
    
    // Validity checks
    File file = new File(path);
    String errorMessage = isValidSearchRequest(file, lastN);
    // Should be a custom error class in the future
    if (errorMessage != null) {
      logger.error(errorMessage);
      return ResponseEntity.badRequest().body(Arrays.asList(errorMessage));
    }
       
    logger.info("All validations passed. Starting to fetch logs from file {}.", fileName);
    List<String> logs = logViewService.getLogs(file, queryString, lastN);

    return ResponseEntity.ok().body(logs);
  }
 
  private String isValidSearchRequest(File file, String lastN) throws IOException {
    String errorMessage = isValidLogRequest(file);
    if (errorMessage != null && lastN != null) {
      try {
        Integer.parseInt(lastN);
      } catch (NumberFormatException e) {
        errorMessage = "The number specified to retrieve the last n records " + lastN + " is not valid.";
      }
    }
    
    return errorMessage;
  }

  private String isValidLogRequest(File file) throws IOException {
    String errorMessage = null;
    // Logging the errors for now. Not throwing an exception.
    // Can be added if needed for NR metrics depending on reporting needs.
    if (!file.exists()) {
      errorMessage = "The requested log file " + file.getAbsolutePath() + " does not exist.";
    } else if (file.isDirectory()) {
      errorMessage = "The requested log file " + file.getAbsolutePath() + " is a directory.";
    } else if (isBinaryFile(file)) {
      errorMessage = "The requested log file " + file.getAbsolutePath() + " is not a text file.";
    }
    
    return errorMessage;
    
  }

  // A somewhat simplistic way of checking if its a log file, based on extension alone
  // Will do for now, but this really just a hack
  private boolean isBinaryFile(File file) throws IOException {
    FileNameMap fileNameMap = URLConnection.getFileNameMap();
    String fileName = file.getName();
    String mimeType = fileNameMap.getContentTypeFor(fileName);
    if ((mimeType == null && isLogFile(fileName)) || mimeType .startsWith("text")) {
      return false;
    }

    return true;
  }
  
  private boolean isLogFile(String fileName) {
    return (fileName.endsWith("out") || fileName.endsWith("log"));
  }

}
