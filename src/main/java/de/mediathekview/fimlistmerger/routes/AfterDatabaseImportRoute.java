package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.persistence.FilmlistMerge;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AfterDatabaseImportRoute extends RouteBuilder {
  public static final String ROUTE_ID = "AfterDatabaseImportRoute";
  public static final String DIRECT_AFTER_DATABASE_IMPORT = "direct:afterDatabaseImportRoute";
  
  private final FilmlistMerge filmlistMerge;

  @Override
  public void configure() {
    from(DIRECT_AFTER_DATABASE_IMPORT)
        .routeId(ROUTE_ID)
        .bean(filmlistMerge, "removeOldEntries")
        .log("Removed old entries")
        ;
  }

}
