package de.mediathekview.fimlistmerger.processors;

import de.mediathekview.fimlistmerger.persistence.Film;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.Sender;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    properties = {
      "camel.springboot.java-routes-include-pattern=**/"
          + PersistenceFilmsToFilmlistProcessorTest.ROUTE_ID
          + "*",
      "spring.liquibase.enabled=false"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableRouteCoverage
class PersistenceFilmsToFilmlistProcessorTest {
  static final String ROUTE_ID = "persistenceFilmsToFilmlistProcessorTestRoute";
  private static final String TEST_ROUTE_FROM = "direct:producer";
  private static final String TEST_ROUTE_TO = "mock:direct:result";
  @Inject CamelContext camelContext;

  @EndpointInject(TEST_ROUTE_TO)
  MockEndpoint mockEndpoint;

  @Inject PersistenceFilmsToFilmlistProcessor persistenceFilmsToFilmlistProcessor;

  @Produce(TEST_ROUTE_FROM)
  private ProducerTemplate template;

  @BeforeEach
  void setUpRouteUnderTest() throws Exception {
    camelContext.addRoutes(
        new RouteBuilder() {
          @Override
          public void configure() {
            from(TEST_ROUTE_FROM).process(persistenceFilmsToFilmlistProcessor).to(TEST_ROUTE_TO);
          }
        });
    camelContext.start();
  }

  @Test
  void persistenceFilms_processPersistenceFilmsToFilmlist_filmlist() throws InterruptedException {
    // GIVEN
    UUID firstTestFilmUUID = UUID.randomUUID();
    LocalDateTime firstTestFilmTime = LocalDateTime.now();

    UUID secondTestFilmUUID = UUID.randomUUID();
    LocalDateTime secondTestFilmTime = LocalDateTime.now();

    Set<Film> persistenceFilms =
        createTestPersistenceFilms(
            firstTestFilmUUID, firstTestFilmTime, secondTestFilmUUID, secondTestFilmTime);

    Map<UUID, de.mediathekview.mlib.daten.Film> expectedFilmlistFilms =
        createTestFilmlistFilms(
            firstTestFilmUUID, firstTestFilmTime, secondTestFilmUUID, secondTestFilmTime);

    // WHEN
    mockEndpoint.expectedMessageCount(1);
    template.sendBody(persistenceFilms);

    // THEN
    mockEndpoint.assertIsSatisfied();
    Optional<Filmlist> optionalFilmlist =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Filmlist.class))
            .findFirst();

    assertThat(optionalFilmlist).isPresent();
    Filmlist filmlist = optionalFilmlist.get();
    assertThat(filmlist.getFilms()).hasSize(2);
    assertThat(filmlist.getLivestreams()).isEmpty();
    assertThat(filmlist.getPodcasts()).isEmpty();

    assertThat(filmlist.getFilms()).containsExactlyInAnyOrderEntriesOf(expectedFilmlistFilms);
  }

  @NotNull
  private Set<Film> createTestPersistenceFilms(
      UUID firstTestFilmUUID,
      LocalDateTime firstTestFilmTime,
      UUID secondTestFilmUUID,
      LocalDateTime secondTestFilmTime) {
    return Set.of(
        Film.builder()
            .uuid(firstTestFilmUUID)
            .sender(Sender.FUNK)
            .titel("First Test Film - persistenceFilms_processPersistenceFilmsToFilmlist_filmlist")
            .thema("PersistenceFilmsToFilmlistProcessorTest")
            .time(firstTestFilmTime)
            .duration(Duration.ofMinutes(13))
            .beschreibung("A test film to test the persistence films to filmlist processor")
            .build(),
        Film.builder()
            .uuid(secondTestFilmUUID)
            .sender(Sender.PHOENIX)
            .titel("Second Test Film - persistenceFilms_processPersistenceFilmsToFilmlist_filmlist")
            .thema("PersistenceFilmsToFilmlistProcessorTest")
            .time(secondTestFilmTime)
            .duration(Duration.ofMinutes(31))
            .beschreibung("A test film to test the persistence films to filmlist processor")
            .build());
  }

  @NotNull
  private Map<UUID, de.mediathekview.mlib.daten.Film> createTestFilmlistFilms(
      UUID firstTestFilmUUID,
      LocalDateTime firstTestFilmTime,
      UUID secondTestFilmUUID,
      LocalDateTime secondTestFilmTime) {
    Map<UUID, de.mediathekview.mlib.daten.Film> expectedFilmlistFilms = new HashMap<>();
    de.mediathekview.mlib.daten.Film firstTestFilm =
        new de.mediathekview.mlib.daten.Film(
            firstTestFilmUUID,
            Sender.FUNK,
            "First Test Film - persistenceFilms_processPersistenceFilmsToFilmlist_filmlist",
            "PersistenceFilmsToFilmlistProcessorTest",
            firstTestFilmTime,
            Duration.ofMinutes(13));
    firstTestFilm.setNeu(false);
    firstTestFilm.setBeschreibung(
        "A test film to test the persistence films to filmlist processor");
    expectedFilmlistFilms.put(firstTestFilmUUID, firstTestFilm);

    de.mediathekview.mlib.daten.Film secondTestFilm =
        new de.mediathekview.mlib.daten.Film(
            secondTestFilmUUID,
            Sender.PHOENIX,
            "Second Test Film - persistenceFilms_processPersistenceFilmsToFilmlist_filmlist",
            "PersistenceFilmsToFilmlistProcessorTest",
            secondTestFilmTime,
            Duration.ofMinutes(31));
    secondTestFilm.setNeu(true);
    secondTestFilm.setBeschreibung(
        "A test film to test the persistence films to filmlist processor");
    expectedFilmlistFilms.put(secondTestFilmUUID, secondTestFilm);
    return expectedFilmlistFilms;
  }
}
