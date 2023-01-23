package com.ag.logviewer.restservice.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogViewService {

  private static final Logger logger = LoggerFactory.getLogger(LogViewService.class);

  /*
   * This method uses a BufferedReader for improved performance. It the flips the order using stack.
   * This is more performant than the other methods I tried. I tried using a RandomFileAccess, but
   * did not get enough time to add buffer, which means it will be very slow for large file.
   */
  public List<String> getLogsNatively(String path) throws IOException {
    Stack<String> logs = new Stack<String>();
    try (FileReader reader = new FileReader(path); 
        BufferedReader br = new BufferedReader(reader)) {

      // read line by line
      String line;
      while ((line = br.readLine()) != null) {
        logs.push(line);
      }

    } catch (IOException e) {
      // Send to NR or other monitoring application as well here
      logger.error("Hit an exception {}, while reading the log file {}", e.getMessage(), path);
      // TODO: create LogViewException
      throw e;
    }

    Collections.reverse(logs);
    return logs;
  }

  /**
   * Method to read files in reverse order.
   * 
   * @param file
   * @return the logs as a list
   * @throws IOException
   */
  // Uses apache commons library
  public List<String> getLogs(File file) throws IOException {
    List<String> logs = new ArrayList<>();
    try (ReversedLinesFileReader reader =
        new ReversedLinesFileReader(file, StandardCharsets.UTF_8)) {

      String line = null;
      while ((line = reader.readLine()) != null) {
        logs.add(line);
      }
    } catch (IOException e) {
      // Send to NR or other monitoring application as well here
      logger.error("Hit an exception {}, while reading the log file {}", 
          e.getMessage(),
          file.getAbsolutePath());
      // TODO: create LogViewException
      throw e;
    }

    return logs;
  }

  // Assumption is we will only run this for Linux systems as stated in the problem description
  public List<String> getLogs(File file, String searchToken, String lastN) throws IOException {
    List<String> logs = new ArrayList<>();
    
    // TODO; this should either be page length or n result
    int lastNLines = Integer.MAX_VALUE;
    if (lastN != null) {
      lastNLines = Integer.parseInt(lastN);
    }
    ProcessBuilder builder = new ProcessBuilder("grep", "-rni", searchToken, file.getAbsolutePath());

    builder.redirectErrorStream(true);
    Process p = builder.start();
    BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line;
    //while (true) {
    while (lastNLines > 0) {
      line = r.readLine();
      if (line == null) {
        break;
      }

      logs.add(line);
      lastNLines--;
    }
    
    return logs;
  }

}
