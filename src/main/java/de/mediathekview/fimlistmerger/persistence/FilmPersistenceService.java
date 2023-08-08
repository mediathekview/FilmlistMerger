package de.mediathekview.fimlistmerger.persistence;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional
public class FilmPersistenceService {
  Logger LOG = LoggerFactory.getLogger(FilmPersistenceService.class);
    
  private final FilmRepository filmRepository;

  public <S extends Film> Iterable<S> saveAllMergeIfExists(Iterable<S> entities) {
    //LOG.info("input size " + StreamSupport.stream(entities.spliterator(), false).count());
    // for some reason the reference needs to be handled by us
    entities.forEach(film -> {film.urls.forEach(url -> { url.film = ((Film)film); });});
    Collection<S> modifiedEntities = StreamSupport.stream(entities.spliterator(), false).collect(Collectors.toList());
    //LOG.info("converted size " + modifiedEntities.size());
    updateUuidIfFilmsAlreadyExists(modifiedEntities);
    //LOG.info("updated ids for existing " + modifiedEntities.size());
    Iterable<S> s = filmRepository.saveAll(modifiedEntities);
    //LOG.info("saveAll ids for existing " + modifiedEntities.size());
    return s;
  }

  public <S extends Film> S saveMergeIfExists(S entity) {
    // for some reason the reference needs to be handled by us
    entity.urls.forEach(url -> { url.film = ((Film)entity); });
    updateUuidIfFilmAlreadyExists(entity);
    return filmRepository.save(entity);
  }

  @Transactional(readOnly = true)
  public <S extends Film> void updateUuidIfFilmsAlreadyExists(Collection<S> entities) {
    entities.forEach(this::updateUuidIfFilmAlreadyExists);
  }

  private <S extends Film> void updateUuidIfFilmAlreadyExists(S entity) {
    filmRepository
        .findBySenderAndTitelIgnoreCaseAndThemaIgnoreCaseAndDuration(
            entity.getSender(), entity.getTitel(), entity.getThema(), entity.getDuration())
        .map(Film::getUuid)
        .ifPresent(entity::setUuid);
  }
}
