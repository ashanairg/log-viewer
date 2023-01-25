package com.ag.logviewer.restservice.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Assumes UTF-8 encoding.
 * 
 * @author AG
 *
 */
@Service
public class LogViewService {

  private static final Logger logger = LoggerFactory.getLogger(LogViewService.class);
  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final char NEW_LINE_CHAR = '\n';
  private static final String READ_ONLY_MODE = "r";

  // This can probably go in a FileReader class.
  // Don't see the need now as it is just a couple of basic methods.
  // Performance concerns: This method will keep reading all the lines till it reaches the line number it wants.
  // However, in the current code this cannot be avoided. To overcome this, log file will need to be indexed by line number and stored.
  /**
   * Method to retrieve the log in chronologically descending order.
   * 
   * @param file, the file to be read
   * @param page, page to be retrieve
   * @param size, the page size
   * @return the log lines from the specified file.
   * @throws IOException
   */
  public List<String> getLogsInReverse(File file, int page, int size) throws IOException {
    int readLines = 0;
    int startLine = (page - 1) * size;
    int endLine = page * size;
    List<String> logs = new ArrayList<>();
    StringBuilder builder = new StringBuilder();
    RandomAccessFile randomAccessFile = null;
    try {
        randomAccessFile = new RandomAccessFile(file, READ_ONLY_MODE);
        long fileLength = file.length() - 1;
        // Set the pointer at the last of the file
        randomAccessFile.seek(fileLength);
        for (long offset = fileLength; offset >= 0; offset--) {
            randomAccessFile.seek(offset);
            char c = (char) randomAccessFile.read();
            builder.append(c);
            if(c == NEW_LINE_CHAR){
                readLines++;
                // The file ends with a new line. So this new line becomes the first line read.
                if (readLines >= startLine && !builder.toString().equals(NEW_LINE)) {
                  builder = builder.reverse();
                  logs.add(builder.toString());
                  startLine++;
                  if (startLine == endLine){
                    break;
                  }
                }
                builder = new StringBuilder();
            }
        }
    } catch (IOException e) {
      // Send to NR or other monitoring application as well here
      logger.info("IOException" + e.getMessage() +" occured while reading last n lines");
      // Maybe create a new LogViewException
      throw e;
    } finally {
        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
              // Send to NR or other monitoring application as well here
              logger.info("IOException" + e.getMessage() +" occured while closing the file reading last n lines");
            }
        }
    }
    
    if (!builder.isEmpty()) {
      logs.add(builder.reverse().toString());
    }

   return logs;
  }

  

  // Assumption is we will only run this for Linux systems as stated in the problem description
  // Log filtering based on dates is not possible at this time.
  // For that to happen, logs need to be parsed, normalized and saved, where it can be queried based on the dates.
  /**
   * Method to return log lines with the specified search token.
   *
   * @param file, the file wherein to search
   * @param searchToken
   * @param size
   * @return logs with matching token
   * @throws IOException
   */
  public List<String> getLogs(File file, String searchToken, String size) throws IOException {
    List<String> logs = new ArrayList<>();
    
    // TODO: this should either be page length or n result once pagination is implemented in search.
    // Otherwise, performance will suffer.
    int numberOfLines = Integer.MIN_VALUE;
    if (size != null) {
      numberOfLines = Integer.parseInt(size);
    }
    ProcessBuilder builder = new ProcessBuilder("grep", "-rni", searchToken, file.getAbsolutePath());

    builder.redirectErrorStream(true);
    Process p = builder.start();
    BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line = null;
    
    // If the number of lines are not specified, keep reading till we run out of results
    while (numberOfLines != Integer.MIN_VALUE ? numberOfLines > 0 : true) {
      line = r.readLine();
      if (line == null) {
        break;
      }

      logs.add(line);
      numberOfLines--;
    }
    
    Collections.reverse(logs);
    return logs;
  }

}
