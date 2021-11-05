package de.mediathekview.fimlistmerger;

import org.apache.camel.Body;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import static de.mediathekview.fimlistmerger.Format.*;

@Component
public class InputFileFormatDetection {
  private static final Logger LOG = LoggerFactory.getLogger(InputFileFormatDetection.class);
  private static final String OLD_FORMAT_HEAD_PATTERN = "\\{\\W*\"Filmliste\":\\s\\[.*";
  private static final String NEW_FORMAT_HEAD_PATTERN = "\\{\\W*\"films\":\\s*\\{.*";
  private static final String JSON_FILE_SUFFIX = ".json";
  private static final int FILE_HEAD_LINE_LIMIT = 2;

  @NotNull
  public Format checkFileType(@Body File file) {
    if (!file.getName().toLowerCase().endsWith(JSON_FILE_SUFFIX)) {
      return UNKNOWN;
    }

    try (Stream<String> linesStream = Files.lines(file.toPath())) {
      // Takes the first two lines and concat them into one
      String fileHead = linesStream.limit(FILE_HEAD_LINE_LIMIT).reduce("", String::concat);

      if (fileHead.matches(OLD_FORMAT_HEAD_PATTERN)) {
        return OLD;
      }

      if (fileHead.matches(NEW_FORMAT_HEAD_PATTERN)) {
        return NEW;
      }

    } catch (IOException ioException) {
      LOG.error(
          "An error IO appeared while determining the file type of {}. Please check the file permissions!",
          file.getName(),
          ioException);
    }

    return UNKNOWN;
  }
}
