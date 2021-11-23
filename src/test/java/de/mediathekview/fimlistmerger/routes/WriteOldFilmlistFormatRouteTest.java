package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.FilmlistTestData;
import de.mediathekview.mlib.daten.Filmlist;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import static de.mediathekview.fimlistmerger.routes.WriteOldFilmlistFormatRoute.ROUTE_ID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    properties = {
      "camel.springboot.java-routes-include-pattern=**/" + ROUTE_ID + "*",
      "spring.liquibase.enabled=false",
      "filmlistmerger.output.file=target/WriteOldFilmlistFormatRouteTest.json"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableRouteCoverage
@SuppressWarnings("unused")
class WriteOldFilmlistFormatRouteTest {
  @Inject CamelContext camelContext;

  @EndpointInject("mock:direct:result")
  MockEndpoint mockEndpoint;

  @Produce("direct:producer")
  private ProducerTemplate template;

  private Path awaitedFilmlistPath;

  @BeforeEach
  void setUp() throws Exception {
    awaitedFilmlistPath =
        Paths.get(ClassLoader.getSystemResource("input/TestFilmlistOld.json").toURI());
  }

  @Test
  @DisplayName("Tests if a filmlist in the old format is correctly written")
  void writeOldFilmlistFormat_filmlist_filmlistInOldFormatWritten()
      throws MalformedURLException, InterruptedException {
    // given
    Filmlist filmlist =
        new Filmlist(
            UUID.fromString("5f330449-c9d2-4b67-89b4-75373b38b9f8"),
            LocalDateTime.parse("2019-10-20T20:04:00"));
    filmlist.addAllFilms(FilmlistTestData.createFilme());

    // when
    template.sendBody(WriteOldFilmlistFormatRoute.ROUTE_FROM, filmlist);

    // then
    mockEndpoint.assertIsSatisfied();
    assertThat(Paths.get("target/WriteOldFilmlistFormatRouteTest.json").toFile())
        .hasSameTextualContentAs(awaitedFilmlistPath.toFile());
  }
}
