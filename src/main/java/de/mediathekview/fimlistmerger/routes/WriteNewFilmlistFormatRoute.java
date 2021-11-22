package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.mlib.daten.Filmlist;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class WriteNewFilmlistFormatRoute extends RouteBuilder {
  public static final String ROUTE_ID = "WriteNewFilmlistFormatRoute";
  public static final String ROUTE_FROM = "direct:writeNewFilmlistFormat";
  private final Path outputFilePath;

  WriteNewFilmlistFormatRoute(@Value("${filmlistmerger.output.file}") Path outputFilePath) {
    this.outputFilePath = outputFilePath;
  }

  @Override
  public void configure() throws Exception {
    from(ROUTE_FROM)
        .routeId(ROUTE_ID)
        .marshal()
        .json(Filmlist.class)
        .to(
            "file://"
                + outputFilePath.getParent().toAbsolutePath()
                + "?fileName="
                + outputFilePath.getFileName()
                + "&charset=utf-8&noop=true");
  }
}
