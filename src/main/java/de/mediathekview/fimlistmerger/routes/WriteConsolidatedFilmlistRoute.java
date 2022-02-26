package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.Format;
import de.mediathekview.fimlistmerger.Metrics;
import de.mediathekview.fimlistmerger.UnknownFilmlistFormatException;
import de.mediathekview.fimlistmerger.persistence.FilmRepository;
import de.mediathekview.fimlistmerger.processors.PersistenceFilmsToFilmlistProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.micrometer.MicrometerConstants;
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
        .log(LoggingLevel.INFO, "Reading all films from the DB...")
        .setBody()
        .method(filmRepository, "findAll")
        .process(persistenceFilmsToFilmlistProcessor)
        .log(LoggingLevel.INFO, "... finished reading from DB")
        .id(WRITE_CONSOLIDATED_FILMLIST_PERSISTENCE_FILMS_TO_FILMLIST_PROCESSOR)
        .setHeader(MicrometerConstants.HEADER_COUNTER_INCREMENT, simple("${body.getFilms().keySet().size()}"))
        .to(Metrics.SUMMARY_MERGED_FILMS_WRITE.toString())
        .to(Metrics.TIMER_WRITE_MERGED_FILMS_START.toString())
        .onCompletion()
        .to(Metrics.TIMER_WRITE_MERGED_FILMS_STOP.toString())
        .end()
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
        .throwException(new UnknownFilmlistFormatException())
        .end()
        .onCompletion()
        .log(LoggingLevel.INFO, "Finished writing. Waiting for new files to import...");
  }
}
