package de.mediathekview.fimlistmerger.dataformat.oldfilmlist;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.filmlisten.reader.FilmlistOldFormatReader;
import de.mediathekview.mlib.filmlisten.writer.FilmlistOldFormatWriter;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spi.DataFormatName;
import org.apache.camel.support.ExchangeHelper;
import org.apache.camel.support.service.ServiceSupport;

import java.io.InputStream;
import java.io.OutputStream;

public class OldFilmlistDataFormat extends ServiceSupport implements DataFormat, DataFormatName {
  @Override
  public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
    new FilmlistOldFormatWriter().write(ExchangeHelper.convertToMandatoryType(exchange, Filmlist.class, graph), stream);
  }

  @Override
  public Object unmarshal(Exchange exchange, InputStream stream) {
    return new FilmlistOldFormatReader().read(stream).orElse(new Filmlist());
  }

  @Override
  public String getDataFormatName() {
    return "oldfilmlist";
  }
}
