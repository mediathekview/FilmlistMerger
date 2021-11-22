package de.mediathekview.fimlistmerger.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class WriteNewFilmlistFormatRoute extends RouteBuilder {
  public static final String ROUTE_ID = "WriteNewFilmlistFormatRoute";
  public static final String ROUTE_FROM = "direct:writeNewFilmlistFormat";

  @Override
  public void configure() throws Exception {}
}
