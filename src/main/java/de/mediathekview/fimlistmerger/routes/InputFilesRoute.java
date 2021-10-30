package de.mediathekview.fimlistmerger.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class InputFilesRoute extends RouteBuilder {
    public static final String ROUTE_ID = "InputFilesRoute";
    public static final String NEW_FILM_FORMAT_ROUTING_TARGET = "newFilmFormatRoutingTarget";

    @Override
    public void configure()  {
    from("file://input?charset=utf-8")
            .routeId(ROUTE_ID)
            .log("Found file ${header.CamelFileName}")
            .to(ReadNewFilmlistFormatRoute.DIRECT_SPLIT_NEW_FILMLIST_TO_FILMS).id(NEW_FILM_FORMAT_ROUTING_TARGET);
    }
}
