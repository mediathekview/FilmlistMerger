package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.Format;
import de.mediathekview.fimlistmerger.InputFileFormatDetection;
import de.mediathekview.fimlistmerger.UnknownFilmlistFormatException;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class SwitchOnFilmlistFormatRoute extends RouteBuilder {
  public static final String ROUTE_ID = "SwitchOnFilmlistFormatRoute";
  public static final String DIRECT_SWITCH_ON_FILMLIST_FORMAT = "direct:switchOnFilmlistFormat";
  public static final String NEW_FILM_FORMAT_ROUTING_TARGET = "writeNewFilmFormatRoutingTarget";
  public static final String OLD_FILM_FORMAT_ROUTING_TARGET = "writeOldFilmFormatRoutingTarget";

  @Override
  public void configure() {
    from(DIRECT_SWITCH_ON_FILMLIST_FORMAT)
        .routeId(ROUTE_ID)
        .log(LoggingLevel.INFO, "Deciding which file format the file ${header.CamelFileName} has.")
        .choice()
        .when(exchange -> isFilmlistFormat(exchange, Format.NEW))
        .log(LoggingLevel.INFO, "Decided ${header.CamelFileName} is in the new format.")
        .to(ReadNewFilmlistFormatRoute.DIRECT_SPLIT_NEW_FILMLIST_TO_FILMS)
        .id(NEW_FILM_FORMAT_ROUTING_TARGET)
        .when(exchange -> isFilmlistFormat(exchange, Format.OLD))
        .log(LoggingLevel.INFO, "Decided ${header.CamelFileName} is in the old format.")
        .to(ReadOldFilmlistFormatRoute.DIRECT_READ_OLD_FILMLIST)
        .id(OLD_FILM_FORMAT_ROUTING_TARGET)
        .otherwise()
        .throwException(new UnknownFilmlistFormatException());
  }

  private boolean isFilmlistFormat(Exchange exchange, Format format) {
    return format
        == new InputFileFormatDetection().checkFileType(exchange.getIn().getBody(File.class));
  }
}
