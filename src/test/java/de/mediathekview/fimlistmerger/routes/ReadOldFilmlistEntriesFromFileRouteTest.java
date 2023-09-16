package de.mediathekview.fimlistmerger.routes;

import static de.mediathekview.fimlistmerger.routes.ConvertOldFilmlistEntryToFilmRoute.DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE;
import static de.mediathekview.fimlistmerger.routes.ReadOldFilmlistEntriesFromFileRoute.DIRECT_READ_OLD_FILMLIST_ENTRIES_FROM_FILE;
import static de.mediathekview.fimlistmerger.routes.ReadOldFilmlistEntriesFromFileRoute.ROUTE_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    properties = {
      "camel.springboot.java-routes-include-pattern=**/" + ROUTE_ID + "*",
      "spring.liquibase.enabled=false"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableRouteCoverage
@DisplayNameGeneration(ReplaceUnderscores.class)
class ReadOldFilmlistEntriesFromFileRouteTest {

  @Autowired
  CamelContext camelContext;

  @EndpointInject("mock:direct:result")
  MockEndpoint mockEndpoint;

  @TempDir File tempDir;
  File tempInputFile;

  @Produce("direct:producer")
  private ProducerTemplate template;

  @BeforeEach
  void setUp() throws Exception {
    setUpRouteUnderTest();
    tempInputFile = File.createTempFile("test", ".json", tempDir);
    Files.copy(
        Paths.get(ClassLoader.getSystemResource("input/TestFilmlistOld.json").toURI()),
        tempInputFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING);
  }

  private void setUpRouteUnderTest() throws Exception {
    AdviceWith.adviceWith(
        camelContext,
        ROUTE_ID,
        advice ->
            advice
                .weaveByToUri(DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE)
                .replace()
                .to(mockEndpoint));
  }

  @Test
  void file_with_old_filmlist_entries_all_entries_read_as_single_messages()
      throws InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(3);

    // when
    template.sendBody(DIRECT_READ_OLD_FILMLIST_ENTRIES_FROM_FILE, tempInputFile);

    // then
    mockEndpoint.assertIsSatisfied();
    final List<String[]> receivedEntries =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(String[].class))
            .toList();

    assertThat(receivedEntries)
        .usingRecursiveComparison()
        .asList()
        .containsExactlyInAnyOrder(
            new String[] {
              "ARD",
              "TestThema",
              "TestTitel",
              "01.01.2017",
              "23:55:00",
              "00:10:00",
              "2",
              "Test beschreibung.",
              "http://example.org/Test.mp4",
              "http://www.example.org/",
              "",
              "",
              "19|klein.mp4",
              "",
              "19|hd.mp4",
              "",
              "1483311300",
              "",
              "",
              "false"
            },
            new String[] {
              "ARD",
              "TestThema2",
              "TestTitel",
              "01.01.2018",
              "23:54:00",
              "00:10:00",
              "2",
              "Test beschreibung.",
              "http://example.org/Test.mp4",
              "http://www.example.org/",
              "",
              "",
              "19|klein.mp4",
              "",
              "19|hd.mp4",
              "",
              "1514847240",
              "",
              "",
              "false"
            },
            new String[] {
              "BR",
              "TestThema2",
              "TestTitel",
              "01.01.2017",
              "23:55:00",
              "00:10:00",
              "2",
              "Test beschreibung.",
              "http://example.org/Test.mp4",
              "http://www.example.org/",
              "",
              "",
              "19|klein.mp4",
              "",
              "19|hd.mp4",
              "",
              "1483311300",
              "",
              "",
              "false"
            });
  }
}
