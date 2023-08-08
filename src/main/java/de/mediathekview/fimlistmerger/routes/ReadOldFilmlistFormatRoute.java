package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.FilmlistSplitterBean;
import de.mediathekview.fimlistmerger.Metrics;
import de.mediathekview.fimlistmerger.dataformat.oldfilmlist.OldFilmlistDataFormat;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.micrometer.MicrometerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReadOldFilmlistFormatRoute extends RouteBuilder {
  public static final String ROUTE_ID = "ReadOldFilmlistFormatRoute";
  public static final String DIRECT_READ_OLD_FILMLIST = "direct:readOldFilmlist";
  public static final String SINGLE_OLD_FORMAT_FILM_ROUTING_TARGET =
      "singleOldFormatFilmRoutingTarget";

  @Autowired
  public FilmlistSplitterBean filmlistSplitterBean;
  
  @Override
  public void configure() {
    from(DIRECT_READ_OLD_FILMLIST)
        .routeId(ROUTE_ID)
        .to(Metrics.TIMER_READ_FILMS_OLD_FORMAT_START.toString())
        .onCompletion()
        .to(Metrics.TIMER_READ_FILMS_OLD_FORMAT_STOP.toString())
        .end()
        .unmarshal(new OldFilmlistDataFormat())
        .log(
            LoggingLevel.INFO,
            "Finished reading old filmlist with ${body.getFilms().keySet().size()} entries, now splitting & importing.")
        .setHeader(
            MicrometerConstants.HEADER_COUNTER_INCREMENT,
            simple("${body.getFilms().keySet().size()}"))
        .to(Metrics.COUNTER_READ_FILMS_OLD_FORMAT_MAX.toString())
        .process(filmlistSplitterBean)
        .log(LoggingLevel.INFO, "Old filmlist make unique: ${body.size()}")
        .split(body())
        .streaming()
        .parallelProcessing()
        //.log(LoggingLevel.INFO, "Old filmlist aggregated: ${body.size()}")
        .to(FilmToDatabaseTargetRoute.ROUTE_FROM)
        .id(SINGLE_OLD_FORMAT_FILM_ROUTING_TARGET);
  }
}
