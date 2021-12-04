package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.mlib.daten.Film;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ReadNewFilmlistFormatRoute extends RouteBuilder {
  public static final String ROUTE_ID = "ReadNewFilmlistFormatRoute";
  public static final String DIRECT_SPLIT_NEW_FILMLIST_TO_FILMS = "direct:splitNewFilmlistToFilms";
  public static final String SINGLE_NEW_FORMAT_FILM_ROUTING_TARGET =
      "singleNewFormatFilmRoutingTarget";
  protected static final String READ_FILMS_FROM_NEW_FILMLIST_JSON_PATH =
      "readFilmsFromNewFilmlistJsonPath";

  @Override
  public void configure() {
    from(DIRECT_SPLIT_NEW_FILMLIST_TO_FILMS)
        .routeId(ROUTE_ID)
        .split()
        .jsonpathWriteAsString("$.films[*]")
        .streaming().parallelProcessing()
        .id(READ_FILMS_FROM_NEW_FILMLIST_JSON_PATH)
        .log(LoggingLevel.DEBUG,"Old body: ${body}")
        .unmarshal()
        .json(Film.class)
        .log(LoggingLevel.DEBUG,"New body: ${body}")
        .to(FilmToDatabaseTargetRoute.ROUTE_FROM)
        .id(SINGLE_NEW_FORMAT_FILM_ROUTING_TARGET);
  }
}
