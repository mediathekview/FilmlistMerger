package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.mlib.daten.Film;
import org.apache.camel.*;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import static de.mediathekview.fimlistmerger.routes.ReadOldFilmlistFormatRoute.ROUTE_ID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    properties = {
      "camel.springboot.java-routes-include-pattern=**/" + ROUTE_ID + "*",
      "spring.liquibase.enabled=false"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableRouteCoverage
class ReadOldFilmlistFormatRouteTest {
  @Inject CamelContext camelContext;

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
                .weaveById(ReadOldFilmlistFormatRoute.SINGLE_OLD_FORMAT_FILM_ROUTING_TARGET)
                .replace()
                .to(mockEndpoint));
  }

  @Test
  @DisplayName("Tests if a filmlist in the old format is correctly read")
  void readOldFilmlistFormat_filmlistOldFormat_AllFilmsRead() throws InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(3);

    // when
    template.sendBody(ReadOldFilmlistFormatRoute.DIRECT_READ_OLD_FILMLIST, tempInputFile);

    // then
    mockEndpoint.assertIsSatisfied();
    final List<Film> receivedFilms =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Film.class))
            .collect(Collectors.toList());

    assertThat(receivedFilms).hasSize(3);
  }
}
