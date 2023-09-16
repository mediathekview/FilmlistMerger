package de.mediathekview.fimlistmerger.routes;

import static de.mediathekview.fimlistmerger.routes.WriteNewFilmlistFormatRoute.ROUTE_ID;
import static org.assertj.core.api.Assertions.assertThat;

import de.mediathekview.fimlistmerger.FilmlistTestData;
import de.mediathekview.mlib.daten.Filmlist;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    properties = {
      "camel.springboot.java-routes-include-pattern=**/" + ROUTE_ID + "*",
      "spring.liquibase.enabled=false",
      "filmlistmerger.output.file=target/WriteNewFilmlistFormatRouteTest.json"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableRouteCoverage
@SuppressWarnings("unused")
class WriteNewFilmlistFormatRouteTest {
  @Autowired CamelContext camelContext;

  @EndpointInject("mock:direct:result")
  MockEndpoint mockEndpoint;

  @Produce("direct:producer")
  private ProducerTemplate template;

  private Path awaitedFilmlistPath;

  @BeforeEach
  void setUp() throws Exception {
    awaitedFilmlistPath =
        Paths.get(ClassLoader.getSystemResource("input/TestFilmlistNew.json").toURI());
  }

  @Test
  @DisplayName("Tests if a filmlist in the new format is correctly written")
  void writeNewFilmlistFormat_filmlist_filmlistInNewFormatWritten()
      throws MalformedURLException, InterruptedException {
    // given
    Filmlist filmlist =
        new Filmlist(
            UUID.fromString("d8bfc10e-14f6-4dae-b5f7-7807f44999a5"),
            LocalDateTime.parse("2021-10-31T09:22:44"));
    filmlist.addAllFilms(FilmlistTestData.createFilme());

    // when
    template.sendBody(WriteNewFilmlistFormatRoute.ROUTE_FROM, filmlist);

    // then
    mockEndpoint.assertIsSatisfied();
    assertThat(Paths.get("target/WriteNewFilmlistFormatRouteTest.json").toFile())
        .hasSameTextualContentAs(awaitedFilmlistPath.toFile());
  }
}
