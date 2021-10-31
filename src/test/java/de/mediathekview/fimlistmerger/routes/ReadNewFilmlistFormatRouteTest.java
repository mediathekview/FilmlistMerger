package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.FilmlistTestData;
import org.apache.camel.*;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

import static de.mediathekview.fimlistmerger.routes.ReadNewFilmlistFormatRoute.ROUTE_ID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"camel.springboot.java-routes-include-pattern=**/" + ROUTE_ID + "*"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableRouteCoverage
class ReadNewFilmlistFormatRouteTest {
  @Autowired CamelContext camelContext;

  @EndpointInject("mock:direct:result")
  MockEndpoint mockEndpoint;

  @TempDir
  File tempDir;
  File tempInputFile;

  @Produce("direct:producer")
  private ProducerTemplate template;

    @BeforeEach
    void setUp() throws Exception {
      setUpRouteUnderTest();
      tempInputFile = File.createTempFile("test", ".json", tempDir);
      Files.copy(Paths.get(ClassLoader.getSystemResource("input/TestFilmlistNew.json").toURI()), tempInputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void setUpRouteUnderTest() throws Exception {
      AdviceWith.adviceWith(
              camelContext,
              ROUTE_ID,
              advice -> advice.weaveById(ReadNewFilmlistFormatRoute.SINGLE_FILM_ROUTING_TARGET).replace().to(mockEndpoint));
    }

  @Test
  @DisplayName("Tests if a simple film json is converted to a valid film object")
  void readNewFilmlistFormat_simpleFilmJson_ValidFilmObject() throws Exception {
    // given

    // when
    template.sendBody(ReadNewFilmlistFormatRoute.DIRECT_SPLIT_NEW_FILMLIST_TO_FILMS, """
      {
        "films": [
          {
            "titel":"Ein einfacher Titel um die Objekt Erzeugung zu testen"
          }
        ]
      }
    """);

    // then
    mockEndpoint.assertIsSatisfied();
    assertThat( mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(Message::getBody)
            .collect(Collectors.toList())).hasSize(1)
            .extracting("titel")
            .containsOnly("Ein einfacher Titel um die Objekt Erzeugung zu testen");
  }

  @Test
  @DisplayName("Tests if a filmlist in the new format is correctly read")
  void readNewFilmlistFormat_filmlistNewFormat_AllFilmsRead()
      throws MalformedURLException, InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(3);

    // when
    template.sendBody(ReadNewFilmlistFormatRoute.DIRECT_SPLIT_NEW_FILMLIST_TO_FILMS, tempInputFile);

    // then
    mockEndpoint.assertIsSatisfied();
    assertThat(
            mockEndpoint.getExchanges().stream()
                .map(Exchange::getIn)
                .map(Message::getBody)
                .collect(Collectors.toList()))
        .containsExactlyInAnyOrderElementsOf(FilmlistTestData.createFilme());
  }
}
