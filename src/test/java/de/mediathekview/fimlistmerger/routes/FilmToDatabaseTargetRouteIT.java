package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.FilmlistMergerApplication;
import de.mediathekview.fimlistmerger.persistence.Film;
import de.mediathekview.fimlistmerger.persistence.FilmRepository;
import de.mediathekview.mlib.daten.Sender;
import org.apache.camel.*;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import jakarta.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static de.mediathekview.fimlistmerger.routes.FilmToDatabaseTargetRoute.ROUTE_ID;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@ContextConfiguration(classes = FilmlistMergerApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(properties = {"camel.springboot.java-routes-include-pattern=**/" + ROUTE_ID + "*"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableRouteCoverage
class FilmToDatabaseTargetRouteIT {
  @Autowired CamelContext camelContext;

  @EndpointInject("mock:direct:result")
  MockEndpoint mockEndpoint;

  @Autowired FilmRepository filmRepository;

  @Produce("direct:producer")
  private ProducerTemplate template;

  @BeforeEach
  void setUp() throws Exception {
    setUpRouteUnderTest();
  }

  private void setUpRouteUnderTest() throws Exception {
    AdviceWith.adviceWith(camelContext, ROUTE_ID, advice -> advice.weaveAddLast().to(mockEndpoint));
  }

  @Test
  @DisplayName("Tests if a film is getting saved to the database")
  void saveFilm_validFilm_FilmSavedToDatabase() throws InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(1);

    de.mediathekview.mlib.daten.Film filmToSave =
        new de.mediathekview.mlib.daten.Film(
            UUID.randomUUID(),
            Sender.PHOENIX,
            "saveFilm_validFilm_FilmSavedToDatabase",
            "FilmToDatabaseTargetRouteIT",
            LocalDateTime.now(),
            Duration.ofMinutes(18));
    // 500 signs lorem ipsum
    filmToSave.setBeschreibung(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et e");

    // when
    template.sendBody(FilmToDatabaseTargetRoute.ROUTE_FROM, filmToSave);

    // then
    mockEndpoint.assertIsSatisfied();
    final Optional<Film> receivedFilm =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Film.class))
            .findAny();

    assertThat(receivedFilm).isPresent();
    assertThat(receivedFilm.get().getUuid()).isEqualTo(filmToSave.getUuid());
    assertThat(filmRepository.findById(receivedFilm.get().getUuid())).isPresent();
  }
}
