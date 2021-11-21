package de.mediathekview.fimlistmerger.processors;

import de.mediathekview.fimlistmerger.FilmPersistenceFilmMapper;
import de.mediathekview.fimlistmerger.persistence.Film;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class FilmToPersistenceFilmProcessor implements Processor {
  private final FilmPersistenceFilmMapper filmPersistenceFilmMapper;

  public FilmToPersistenceFilmProcessor(FilmPersistenceFilmMapper filmPersistenceFilmMapper) {
    this.filmPersistenceFilmMapper = filmPersistenceFilmMapper;
  }

  @Override
  public void process(Exchange exchange) {
    exchange
        .getIn()
        .setBody(
            filmPersistenceFilmMapper.filmToPersistenceFilm(
                exchange.getIn().getBody(de.mediathekview.mlib.daten.Film.class)),
            Film.class);
  }
}
