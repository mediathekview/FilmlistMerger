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
    var incoming = exchange.getIn();
    exchange
        .getIn()
        .setBody(
            incoming.getBody() instanceof de.mediathekview.mlib.daten.Film
                ? filmPersistenceFilmMapper.filmToPersistenceFilm(
                    incoming.getBody(de.mediathekview.mlib.daten.Film.class))
                : incoming.getBody(Film.class),
            Film.class);
  }
}
