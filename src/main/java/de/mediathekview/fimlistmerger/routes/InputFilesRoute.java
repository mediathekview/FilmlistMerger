package de.mediathekview.fimlistmerger.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class InputFilesRoute extends RouteBuilder {
    public static final String ROUTE_ID = "InputFilesRoute";
    @Override
    public void configure()  {
    from("file://input?charset=utf-8")
            .routeId(ROUTE_ID)
            .log("File ${header.CamelFileName} exists");
    }
}
