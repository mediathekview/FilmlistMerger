package de.mediathekview.fimlistmerger.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class WriteOldFilmlistFormatRoute extends RouteBuilder {
  public static final String ROUTE_ID = "WriteOldFilmlistFormatRoute";
  public static final String ROUTE_FROM = "direct:writeOldFilmlistFormat";

  @Override
  public void configure() throws Exception {
    from(ROUTE_FROM).routeId(ROUTE_ID);
  }
}
