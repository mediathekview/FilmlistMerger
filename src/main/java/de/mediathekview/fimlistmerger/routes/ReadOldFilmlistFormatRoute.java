package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.FilmlistSplitterBean;
import de.mediathekview.fimlistmerger.Metrics;
import de.mediathekview.fimlistmerger.dataformat.oldfilmlist.OldFilmlistDataFormat;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ReadOldFilmlistFormatRoute extends RouteBuilder {
  public static final String ROUTE_ID = "ReadOldFilmlistFormatRoute";
  public static final String DIRECT_READ_OLD_FILMLIST = "direct:readOldFilmlist";
  public static final String SINGLE_OLD_FORMAT_FILM_ROUTING_TARGET =
      "singleOldFormatFilmRoutingTarget";

  @Override
  public void configure() {
    from(DIRECT_READ_OLD_FILMLIST)
        .routeId(ROUTE_ID)
        .unmarshal(new OldFilmlistDataFormat())
        .split()
        .method(FilmlistSplitterBean.class)
        .streaming().parallelProcessing()
        .log(LoggingLevel.DEBUG,"Old filmlist film body: ${body}")
        .to(Metrics.COUNTER_READ_FILMS_OLD_FORMAT.toString())
        .to(FilmToDatabaseTargetRoute.ROUTE_FROM)
        .id(SINGLE_OLD_FORMAT_FILM_ROUTING_TARGET);
  }
}
