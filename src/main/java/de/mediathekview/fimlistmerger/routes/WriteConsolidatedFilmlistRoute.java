package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.persistence.FilmRepository;
import de.mediathekview.fimlistmerger.processors.PersistenceFilmsToFilmlistProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class WriteConsolidatedFilmlistRoute extends RouteBuilder {
  public static final String ROUTE_ID = "WriteConsolidatedFilmlistRoute";
  public static final String ROUTE_FROM = "direct:writeConsolidatedFilmlist";

  private final FilmRepository filmRepository;
  private final PersistenceFilmsToFilmlistProcessor persistenceFilmsToFilmlistProcessor;

  public WriteConsolidatedFilmlistRoute(
      FilmRepository filmRepository,
      PersistenceFilmsToFilmlistProcessor persistenceFilmsToFilmlistProcessor) {
    this.filmRepository = filmRepository;
    this.persistenceFilmsToFilmlistProcessor = persistenceFilmsToFilmlistProcessor;
  }

  @Override
  public void configure() {

    from(ROUTE_FROM)
        .routeId(ROUTE_ID)
        .setBody()
        .method(filmRepository, "findAll")
        .process(persistenceFilmsToFilmlistProcessor);
    // TODO split on config property to which format to write and then write a file in the format
    // => New route for this?
  }
}
