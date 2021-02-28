package de.mediathekview.fimlistmerger;

import org.apache.camel.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import static de.mediathekview.fimlistmerger.Format.*;

@Component
public class InputFileDetection {
  private static final Logger LOG = LoggerFactory.getLogger(InputFileDetection.class);
  private static final String OLD_FORMAT_HEAD_PATTERN = "\\{\\W*\"Filmliste\":\\s\\[.*";
  private static final String JSON_FILE_SUFFIX = ".json";

  public Format checkFileType(@Body File file) {
    if(!file.getName().toLowerCase().endsWith(JSON_FILE_SUFFIX))
    {
      return UNKNOWN;
    }

    try(Stream<String> linesStream = Files.lines(file.toPath())) {
        //Takes the first two lines and concat them into one
        String fileHead = linesStream.limit(2).reduce("", String::concat);
        return fileHead.matches(OLD_FORMAT_HEAD_PATTERN) ? OLD : UNKNOWN;
    } catch (IOException ioException) {
      LOG.error(
          "An error IO appeared while determining the file type of {}. Please check the file permissions!",
          file.getName(),
          ioException);
    }
    return null;
  }
}