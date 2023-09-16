package de.mediathekview.fimlistmerger.routes;

import static de.mediathekview.fimlistmerger.routes.WriteConsolidatedFilmlistRoute.ROUTE_ID;
import static org.assertj.core.api.Assertions.assertThat;

import de.mediathekview.fimlistmerger.FilmPersistenceFilmMapper;
import de.mediathekview.fimlistmerger.FilmlistMergerApplication;
import de.mediathekview.fimlistmerger.persistence.Film;
import de.mediathekview.fimlistmerger.persistence.FilmPersistenceService;
import de.mediathekview.mlib.daten.AbstractMediaResource;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.Sender;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.camel.*;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("integration-test")
@ContextConfiguration(classes = FilmlistMergerApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(
    properties = {
      "camel.springboot.java-routes-include-pattern=**/" + ROUTE_ID + "*",
      "filmlistmerger.output.format=OLD"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableRouteCoverage
class WriteConsolidatedFilmlistRouteIT {
  @Autowired
  CamelContext camelContext;

  @EndpointInject("mock:direct:result")
  MockEndpoint mockEndpoint;

  @Autowired FilmPersistenceService filmPersistenceService;
  @Autowired FilmPersistenceFilmMapper filmPersistenceFilmMapper;

  @Produce("direct:producer")
  private ProducerTemplate template;

  @BeforeEach
  void setUpRoute() throws Exception {
    AdviceWith.adviceWith(
        camelContext,
        ROUTE_ID,
        advice -> {
          advice
              .weaveById(WriteConsolidatedFilmlistRoute.OLD_FILM_FORMAT_ROUTING_TARGET)
              .replace()
              .to(mockEndpoint);
        });
  }

  @Test
  @Transactional
  @DisplayName("Tests if the old format writer is called when filmlist format is set to old format")
  void switchFilmlistFormat_oldFormat_oldFormatWriterIsCalled() throws Exception {
    // given
    mockEndpoint.expectedMessageCount(1);

    Set<Film> testFilms = createTestPersistenceFilms();
    filmPersistenceService.saveAllMergeIfExists(testFilms);

    // when
    template.sendBody(WriteConsolidatedFilmlistRoute.ROUTE_FROM, null);

    // then
    mockEndpoint.assertIsSatisfied();
    Optional<Filmlist> optionalFilmlist =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Filmlist.class))
            .findFirst();

    assertThat(optionalFilmlist).isPresent();
    Filmlist filmlist = optionalFilmlist.get();
    assertThat(filmlist.getFilms()).hasSize(testFilms.size());
    assertThat(filmlist.getLivestreams()).isEmpty();
    assertThat(filmlist.getPodcasts()).isEmpty();

    assertThat(filmlist.getFilms())
        .containsExactlyInAnyOrderEntriesOf(
            testFilms.stream()
                .map(filmPersistenceFilmMapper::persistenceFilmToFilm)
                .collect(Collectors.toMap(AbstractMediaResource::getUuid, Function.identity())));
  }

  @NotNull
  private Set<Film> createTestPersistenceFilms() {
    return Set.of(
        Film.builder()
            .uuid(UUID.randomUUID())
            .sender(Sender.KIKA)
            .titel("First Test Film")
            .thema("WriteConsolidatedFilmlistRouteIT")
            .time(LocalDateTime.now())
            .duration(Duration.ofMinutes(16))
            .neu(false)
            .beschreibung("A test film to test write consolidated filmlist route")
            .build(),
        Film.builder()
            .uuid(UUID.randomUUID())
            .sender(Sender.SRF)
            .titel("Second Test Film")
            .thema("WriteConsolidatedFilmlistRouteIT")
            .time(LocalDateTime.now())
            .duration(Duration.ofMinutes(23))
            .neu(true)
            .beschreibung("A test film to test write consolidated filmlist route")
            .build());
  }
}
