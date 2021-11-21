package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.persistence.FilmRepository;
import de.mediathekview.fimlistmerger.processors.FilmToPersistenceFilmProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class FilmToDatabaseTargetRoute extends RouteBuilder {
  public static final String ROUTE_ID = "FilmToDatabaseTargetRoute";
  public static final String ROUTE_FROM = "direct:film2DBRoute";

  private final FilmRepository filmRepository;
  private final FilmToPersistenceFilmProcessor filmToPersistenceFilmProcessor;

  public FilmToDatabaseTargetRoute(
      FilmRepository filmRepository,
      FilmToPersistenceFilmProcessor filmToPersistenceFilmProcessor) {
    this.filmRepository = filmRepository;
    this.filmToPersistenceFilmProcessor = filmToPersistenceFilmProcessor;
  }

  @Override
  public void configure() {

    from(ROUTE_FROM)
        .routeId(ROUTE_ID)
        .process(filmToPersistenceFilmProcessor)
        .bean(filmRepository, "saveMergeIfExists");
  }
}
