package de.mediathekview.fimlistmerger;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class InputFileDetectionTest {

  private static final String TEST_FILE_INPUT_DIRECTORY = "input";

  private static Stream<Arguments> providePathsAndFormats() {
    return Stream.of(
        Arguments.of("TestFilmlistOld.json", Format.OLD),
        Arguments.of("TestFilmlistNew.json", Format.NEW),
        Arguments.of("SimpleJsonFile.json", Format.UNKNOWN),
        Arguments.of("test.txt", Format.UNKNOWN));
  }

  private static Path testFileNameToPath(String fileName) throws URISyntaxException {
    return Paths.get(ClassLoader.getSystemResource(TEST_FILE_INPUT_DIRECTORY).toURI())
        .resolve(fileName);
  }

  @DisplayName("Check if a file brings the expected format")
  @ParameterizedTest(name = "{index} Check if {0} is {1}")
  @MethodSource("providePathsAndFormats")
  void checkFileType_file_determineFileFormat(String testFileName, Format expectedFormat)
      throws URISyntaxException {
    assertThat(new InputFileDetection().checkFileType(testFileNameToPath(testFileName).toFile()))
        .isEqualTo(expectedFormat);
  }

  @Test
  @DisplayName("Check if a non existing file leads brings the unknown format")
  void checkFileType_nonExistingFile_unknownFormat() throws URISyntaxException {
    assertThat(
            new InputFileDetection()
                .checkFileType(testFileNameToPath("NotExistingFile.json").toFile()))
        .isEqualTo(Format.UNKNOWN);
  }

  @Test
  @DisplayName("Check if a non existing file leads to a error log message")
  void checkFileType_nonExistingFile_errorMessage() throws URISyntaxException {
    LogCaptor logCaptor = LogCaptor.forClass(InputFileDetection.class);
    new InputFileDetection().checkFileType(testFileNameToPath("NotExistingFile.json").toFile());
    assertThat(logCaptor.getErrorLogs())
        .containsExactly(
            "An error IO appeared while determining the file type of NotExistingFile.json. Please check the file permissions!");
  }
}
