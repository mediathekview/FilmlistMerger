package de.mediathekview.fimlistmerger.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class FilmToDatabaseTargetRoute extends RouteBuilder {
    public static final String ROUTE_FROM = "direct:film2DBRoute";

    @Override
    public void configure() {


        from(ROUTE_FROM)

                ;


    }
}
