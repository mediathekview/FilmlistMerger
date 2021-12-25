package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.Metrics;
import de.mediathekview.fimlistmerger.persistence.FilmPersistenceService;
import de.mediathekview.fimlistmerger.persistence.FilmRepository;
import de.mediathekview.fimlistmerger.processors.FilmToPersistenceFilmProcessor;
import lombok.RequiredArgsConstructor;
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
            .process(filmToPersistenceFilmProcessor)
            .bean(filmPersistenceService, "saveMergeIfExists")
            .to(Metrics.COUNTER_FILMS_SAVED.toString());
  }
}

