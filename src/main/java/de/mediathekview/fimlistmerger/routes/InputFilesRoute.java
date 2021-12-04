package de.mediathekview.fimlistmerger.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class InputFilesRoute extends RouteBuilder {
  public static final String ROUTE_ID = "InputFilesRoute";
  public static final String SWITCH_ON_FILMLIST_FORMAT_ROUTING_TARGET =
      "switchOnFilmlistFormatRoutingTarget";
  public static final String WRITE_CONSOLIDATED_FILMLIST_ROUTING_TARGET =
      "writeConsolidatedFilmlistRoutingTarget";

  @Override
  public void configure() {
    from("file://input?charset=utf-8")
        .routeId(ROUTE_ID)
        .log(LoggingLevel.DEBUG,"Found file ${header.CamelFileName}")
        .to(SwitchOnFilmlistFormatRoute.DIRECT_SWITCH_ON_FILMLIST_FORMAT)
        .id(SWITCH_ON_FILMLIST_FORMAT_ROUTING_TARGET)
        .onCompletion()
        .onCompleteOnly()
        .to(WriteConsolidatedFilmlistRoute.ROUTE_FROM)
        .id(WRITE_CONSOLIDATED_FILMLIST_ROUTING_TARGET)
        .end();
  }
}
