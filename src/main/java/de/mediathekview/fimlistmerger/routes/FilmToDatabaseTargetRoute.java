package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.Metrics;
import de.mediathekview.fimlistmerger.persistence.Film;
import de.mediathekview.fimlistmerger.persistence.FilmPersistenceService;
import de.mediathekview.fimlistmerger.processors.FilmToPersistenceFilmProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FilmToDatabaseTargetRoute extends RouteBuilder {
  public static final String ROUTE_ID = "FilmToDatabaseTargetRoute";
  public static final String ROUTE_FROM = "direct:film2DBRoute";

  private final FilmPersistenceService filmPersistenceService;
  private final FilmToPersistenceFilmProcessor filmToPersistenceFilmProcessor;

  @Override
  public void configure() {

    from(ROUTE_FROM)
        .routeId(ROUTE_ID)
        .to(Metrics.TIMER_WRITE_FILM_START.toString())
        .onCompletion()
        .to(Metrics.TIMER_WRITE_FILM_STOP.toString())
        .end()
        .log(LoggingLevel.DEBUG,"Converting film to persistence film")
        .process(filmToPersistenceFilmProcessor)
        .process(this::save)
        .log(LoggingLevel.DEBUG,"Saved film")
        .to(Metrics.COUNTER_FILMS_SAVED_CURRENT.toString());
  }

  private void save(Exchange exchange) {
    exchange.getMessage().setBody(filmPersistenceService.saveMergeIfExists(exchange.getMessage().getBody(Film.class)));
  }
}
