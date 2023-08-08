package de.mediathekview.fimlistmerger.processors;

import de.mediathekview.fimlistmerger.FilmPersistenceFilmMapper;
import de.mediathekview.fimlistmerger.persistence.FilmPersistenceService;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FilmToPersistenceFilmProcessor implements Processor {
    Logger LOG = LoggerFactory.getLogger(FilmToPersistenceFilmProcessor.class);
    private final FilmPersistenceFilmMapper filmPersistenceFilmMapper;

  public FilmToPersistenceFilmProcessor(FilmPersistenceFilmMapper filmPersistenceFilmMapper) {
    this.filmPersistenceFilmMapper = filmPersistenceFilmMapper;
  }

  @Override
  public void process(Exchange exchange) {
    ArrayList<de.mediathekview.fimlistmerger.persistence.Film> persistenceFilmList = new ArrayList<>();
    List<de.mediathekview.mlib.daten.Film> crawerFilmList = (List<de.mediathekview.mlib.daten.Film>)exchange.getIn().getBody(List.class);
    crawerFilmList.forEach( film -> {
        persistenceFilmList.add(filmPersistenceFilmMapper.filmToPersistenceFilm( film));
    });
    exchange.getIn().setBody(persistenceFilmList, List.class);
    /*
    exchange
        .getIn()
        .setBody(
            filmPersistenceFilmMapper.filmToPersistenceFilm(
                exchange.getIn().getBody(de.mediathekview.mlib.daten.Film.class)),
            Film.class);*/
  }
}
