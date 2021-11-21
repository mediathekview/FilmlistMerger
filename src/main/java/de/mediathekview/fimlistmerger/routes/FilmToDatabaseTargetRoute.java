package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.persistence.FilmRepository;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FilmToDatabaseTargetRoute extends RouteBuilder {
  public static final String ROUTE_ID = "FilmToDatabaseTargetRoute";
  public static final String ROUTE_FROM = "direct:film2DBRoute";

  @Autowired private FilmRepository filmRepository;

  @Override
  public void configure() {

    from(ROUTE_FROM).routeId(ROUTE_ID).bean(filmRepository, "saveMergeIfExists");
  }
}
