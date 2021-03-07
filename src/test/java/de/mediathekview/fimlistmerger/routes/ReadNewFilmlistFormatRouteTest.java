package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.FilmlistTestData;
import org.apache.camel.*;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.net.MalformedURLException;
import java.util.stream.Collectors;

import static de.mediathekview.fimlistmerger.routes.ReadNewFilmlistFormatRoute.ROUTE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest(properties = {"camel.springboot.java-routes-include-pattern=**/" + ROUTE_ID + "*"})
@EnableRouteCoverage
class ReadNewFilmlistFormatRouteTest {
  @Autowired CamelContext camelContext;

  @EndpointInject("mock:direct:result")
  MockEndpoint mockEndpoint;

  File tempInputFile;

  @Produce("direct:producer")
  private ProducerTemplate template;

  @BeforeEach
  void setUpRouteUnderTest() throws Exception {
    AdviceWith.adviceWith(
        camelContext,
        ROUTE_ID,
        advice -> {
          advice
              .weaveById(ReadNewFilmlistFormatRoute.SINGLE_FILM_ROUTING_TARGET)
              .replace()
              .to(mockEndpoint);
        });
  }

  @Test
  @DisplayName("Tests if a simple film json is converted to a valid film object")
  void readNewFilmlistFormat_simpleFilmJson_ValidFilmObject() throws Exception {
    // given
    AdviceWith.adviceWith(
        camelContext,
        ROUTE_ID,
        advice -> advice
              .weaveById(ReadNewFilmlistFormatRoute.READ_FILMS_FROM_NEW_FILMLIST_JSON_PATH)
              .remove());

    // when
    template.sendBody(ReadNewFilmlistFormatRoute.DIRECT_SPLIT_NEW_FILMLIST_TO_FILMS, """
      {
        "titel":"Ein einfacher Titel um die Objekt erzeugung zu testen"
      }
    """);

    // then
    mockEndpoint.assertIsSatisfied();
    assertThat(mockEndpoint.getExchanges()).hasSize(1)
            .extracting("title")
            .containsOnly(tuple("Ein einfacher Titel um die Objekt erzeugung zu testen"));
  }

  //@Test
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
        .containsExactlyElementsOf(FilmlistTestData.createFilme());
  }
}
