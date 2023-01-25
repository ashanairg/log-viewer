package com.ag.logviewer.restservice.controller;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ag.logviewer.restservice.service.LogViewService;

@RestController
@RequestMapping("/logs")
public class LogViewController {
  private static final Logger logger = LoggerFactory.getLogger(LogViewController.class);

  private final LogViewService logViewService;
  
  @Autowired
  public LogViewController(LogViewService logViewService) {
    this.logViewService = logViewService;
  }

  @GetMapping("/{file}")
  public ResponseEntity<List<String>> viewLogs(
      @PathVariable("file") String fileName,
      @RequestParam("page") int page, 
      @RequestParam("size") int size)
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
    List<String> logs = logViewService.getLogsInReverse(file, page, size);
    return ResponseEntity.ok().body(logs);
  }

  @GetMapping("/{file}/search")
  public ResponseEntity<List<String>> searchLogs(
      @PathVariable("file") String fileName,
      @RequestParam("q") String queryString,
      @RequestParam(required = false, value = "size") String size)
      throws IOException {
    String path = "/var/log/" + fileName; 
    
    // Validity checks
    File file = new File(path);
    String errorMessage = isValidSearchRequest(file, size);
    // Should be a custom error class in the future
    if (errorMessage != null) {
      logger.error(errorMessage);
      return ResponseEntity.badRequest().body(Arrays.asList(errorMessage));
    }
       
    logger.info("All validations passed. Starting to fetch logs from file {}.", fileName);
    List<String> logs = logViewService.getLogs(file, queryString, size);

    return ResponseEntity.ok().body(logs);
  }
 
  private String isValidSearchRequest(File file, String size) throws IOException {
    String errorMessage = isValidLogRequest(file);
    if (errorMessage != null && size != null) {
      try {
        Integer.parseInt(size);
      } catch (NumberFormatException e) {
        errorMessage = "The number specified to retrieve the last n records " + size + " is not valid.";
      }
    }
    
    return errorMessage;
  }

  // Eventually there will be a UI, so not checking page and size values for validity
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
