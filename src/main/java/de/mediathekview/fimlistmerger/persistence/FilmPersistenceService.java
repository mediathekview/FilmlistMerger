package de.mediathekview.fimlistmerger.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class FilmPersistenceService {
  private final FilmRepository filmRepository;

  public <S extends Film> Iterable<S> saveAllMergeIfExists(Iterable<S> entities) {
    Collection<S> modifiedEntities = StreamSupport.stream(entities.spliterator(), true).toList();
    updateUuidIfFilmsAlreadyExists(modifiedEntities);
    return filmRepository.saveAll(modifiedEntities);
  }

  public <S extends Film> S saveMergeIfExists(S entity) {
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
