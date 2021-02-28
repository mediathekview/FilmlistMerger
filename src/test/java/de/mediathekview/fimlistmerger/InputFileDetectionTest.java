package de.mediathekview.fimlistmerger;

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

    @ParameterizedTest(name = "{index} Check if {0} is {1}")
    @MethodSource("providePathsAndFormats")
    void checkFileType_file_determineFileFormat(String testFileName, Format expectedFormat) throws URISyntaxException {
        assertThat(new InputFileDetection().checkFileType(testFileNameToPath(testFileName).toFile())).isEqualTo(expectedFormat);
    }

    private static Stream<Arguments> providePathsAndFormats() {
        return Stream.of(
                Arguments.of("TestFilmlistOld.json",Format.OLD),
                Arguments.of("SimpleJsonFile.json",Format.UNKNOWN),
                Arguments.of("test.txt",Format.UNKNOWN)
        );
    }

    private static Path testFileNameToPath(String fileName) throws URISyntaxException {
        return Paths.get(ClassLoader.getSystemResource(TEST_FILE_INPUT_DIRECTORY).toURI()).resolve(fileName);
    }

}
