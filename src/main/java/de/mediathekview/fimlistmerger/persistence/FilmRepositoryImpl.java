package de.mediathekview.fimlistmerger.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.stream.StreamSupport;

@Component
public class FilmRepositoryImpl {

  @PersistenceContext private EntityManager entityManager;

  @Autowired private FilmRepository filmRepository;

  public <S extends Film> Iterable<S> saveAllMergeIfExists(Iterable<S> entities) {
    Collection<S> modifiedEntities = StreamSupport.stream(entities.spliterator(), true).toList();
    updateUuidIfFilmAlreadyExists(modifiedEntities);
    return filmRepository.saveAll(modifiedEntities);
  }

  private <S extends Film> void updateUuidIfFilmAlreadyExists(Collection<S> entities) {
    entities.forEach(
        entity ->
            filmRepository
                .findBySenderAndTitelIgnoreCaseAndThemaIgnoreCaseAndDuration(
                    entity.getSender(), entity.getTitel(), entity.getThema(), entity.getDuration())
                .map(Film::getUuid)
                .ifPresent(entity::setUuid));
  }
}
