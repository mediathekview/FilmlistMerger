package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.Format;
import de.mediathekview.fimlistmerger.UnknownFilmlistFormatException;
import de.mediathekview.fimlistmerger.persistence.FilmRepository;
import de.mediathekview.fimlistmerger.processors.PersistenceFilmsToFilmlistProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WriteConsolidatedFilmlistRoute extends RouteBuilder {
  public static final String ROUTE_ID = "WriteConsolidatedFilmlistRoute";
  public static final String ROUTE_FROM = "direct:writeConsolidatedFilmlist";
  public static final String WRITE_CONSOLIDATED_FILMLIST_PERSISTENCE_FILMS_TO_FILMLIST_PROCESSOR =
      "writeConsolidatedFilmlistPersistenceFilmsToFilmlistProcessor";
  public static final String NEW_FILM_FORMAT_ROUTING_TARGET = "newFilmFormatRoutingTarget";
  public static final String OLD_FILM_FORMAT_ROUTING_TARGET = "oldFilmFormatRoutingTarget";

  private final FilmRepository filmRepository;
  private final PersistenceFilmsToFilmlistProcessor persistenceFilmsToFilmlistProcessor;
  private final Format filmlistFormat;

  public WriteConsolidatedFilmlistRoute(
      FilmRepository filmRepository,
      PersistenceFilmsToFilmlistProcessor persistenceFilmsToFilmlistProcessor,
      @Value("${filmlistmerger.output.format}") Format filmlistFormat) {
    this.filmRepository = filmRepository;
    this.persistenceFilmsToFilmlistProcessor = persistenceFilmsToFilmlistProcessor;
    this.filmlistFormat = filmlistFormat;
  }

  @Override
  public void configure() {

    from(ROUTE_FROM)
        .routeId(ROUTE_ID)
        .setBody()
        .method(filmRepository, "findAll")
        .process(persistenceFilmsToFilmlistProcessor)
        .id(WRITE_CONSOLIDATED_FILMLIST_PERSISTENCE_FILMS_TO_FILMLIST_PROCESSOR)
        .log(LoggingLevel.INFO, "Deciding in which format to write the consolidated fimlist...")
        .choice()
        .when(exchange -> Format.OLD == filmlistFormat)
        .log(LoggingLevel.INFO, "Decided to write in the old format.")
        .to(WriteOldFilmlistFormatRoute.ROUTE_FROM)
        .id(OLD_FILM_FORMAT_ROUTING_TARGET)
        .when(exchange -> Format.NEW == filmlistFormat)
        .log(LoggingLevel.INFO, "Decided to write in the new format.")
        .to(WriteNewFilmlistFormatRoute.ROUTE_FROM)
        .id(NEW_FILM_FORMAT_ROUTING_TARGET)
        .otherwise()
        .throwException(new UnknownFilmlistFormatException());
  }
}
