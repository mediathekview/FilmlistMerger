package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.mlib.daten.Film;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ReadNewFilmlistFormatRoute extends RouteBuilder {
  public static final String ROUTE_ID = "ReadNewFilmlistFormatRoute";
  public static final String DIRECT_SPLIT_NEW_FILMLIST_TO_FILMS = "direct:splitNewFilmlistToFilms";
  public static final String SINGLE_FILM_ROUTING_TARGET = "singleFilmRoutingTarget";
  protected static final String READ_FILMS_FROM_NEW_FILMLIST_JSON_PATH = "readFilmsFromNewFilmlistJsonPath";

    @Override
  public void configure() {
    from(DIRECT_SPLIT_NEW_FILMLIST_TO_FILMS)
        .routeId(ROUTE_ID)
        .split()
        .jsonpathWriteAsString("$.films[*]").id(READ_FILMS_FROM_NEW_FILMLIST_JSON_PATH)
        .convertBodyTo(Film.class)
        .log("${body}")
        .to(FilmToDatabaseTargetRoute.ROUTE_FROM)
        .id(SINGLE_FILM_ROUTING_TARGET);
  }
}
