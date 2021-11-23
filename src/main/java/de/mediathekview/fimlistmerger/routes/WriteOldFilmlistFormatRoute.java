package de.mediathekview.fimlistmerger.routes;

import de.mediathekview.fimlistmerger.dataformat.oldfilmlist.OldFilmlistDataFormat;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class WriteOldFilmlistFormatRoute extends RouteBuilder {
  public static final String ROUTE_ID = "WriteOldFilmlistFormatRoute";
  public static final String ROUTE_FROM = "direct:writeOldFilmlistFormat";

  private final Path outputFilePath;

  WriteOldFilmlistFormatRoute(@Value("${filmlistmerger.output.file}") Path outputFilePath) {
    this.outputFilePath = outputFilePath;
  }

  @Override
  public void configure() throws Exception {
    from(ROUTE_FROM)
        .routeId(ROUTE_ID)
        .marshal(new OldFilmlistDataFormat())
        .to(
            "file://"
                + outputFilePath.getParent().toAbsolutePath()
                + "?fileName="
                + outputFilePath.getFileName()
                + "&charset=utf-8&noop=true");
  }
}
