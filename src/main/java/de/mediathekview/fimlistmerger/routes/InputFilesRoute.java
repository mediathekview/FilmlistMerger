package de.mediathekview.fimlistmerger.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InputFilesRoute extends RouteBuilder {
  public static final String ROUTE_ID = "InputFilesRoute";
  public static final String SWITCH_ON_FILMLIST_FORMAT_ROUTING_TARGET =
      "switchOnFilmlistFormatRoutingTarget";
  public static final String WRITE_CONSOLIDATED_FILMLIST_ROUTING_TARGET =
      "writeConsolidatedFilmlistRoutingTarget";

  private final String inputFolderPath;

  public InputFilesRoute(@Value("${filmlistmerger.input.path:input}") String inputFolderPath) {
    this.inputFolderPath = inputFolderPath;
  }

  @Override
  public void configure() {
    from("file://" + inputFolderPath + "?charset=utf-8&include=.*.json")
        .routeId(ROUTE_ID)
        .log(LoggingLevel.INFO, "Found file ${header.CamelFileName}")
        .to(SwitchOnFilmlistFormatRoute.DIRECT_SWITCH_ON_FILMLIST_FORMAT)
        .id(SWITCH_ON_FILMLIST_FORMAT_ROUTING_TARGET)
        .onCompletion()
        .onCompleteOnly()
        .log(LoggingLevel.INFO, "Completed reading files, writing consolidated filmlist now.")
        .to(WriteConsolidatedFilmlistRoute.ROUTE_FROM)
        .id(WRITE_CONSOLIDATED_FILMLIST_ROUTING_TARGET)
        .end();
  }
}
