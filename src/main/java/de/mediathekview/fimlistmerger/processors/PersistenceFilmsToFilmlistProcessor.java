package de.mediathekview.fimlistmerger.processors;

import de.mediathekview.fimlistmerger.FilmPersistenceFilmMapper;
import de.mediathekview.mlib.daten.Filmlist;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class PersistenceFilmsToFilmlistProcessor implements Processor {
  final FilmPersistenceFilmMapper filmPersistenceFilmMapper;

  public PersistenceFilmsToFilmlistProcessor(FilmPersistenceFilmMapper filmPersistenceFilmMapper) {
    this.filmPersistenceFilmMapper = filmPersistenceFilmMapper;
  }

  @Override
  public void process(Exchange exchange) {

    Filmlist filmlist = new Filmlist();
    filmlist.addAllFilms(
        exchange.getIn().getBody(FilmSet.class).parallelStream()
            .map(filmPersistenceFilmMapper::persistenceFilmToFilm)
            .collect(Collectors.toSet()));
    exchange.getIn().setBody(filmlist, Filmlist.class);
  }

  private static class FilmSet extends HashSet<de.mediathekview.fimlistmerger.persistence.Film> {}
}
