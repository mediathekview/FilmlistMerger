package de.mediathekview.fimlistmerger.persistence;

import de.mediathekview.mlib.daten.Sender;
import org.springframework.data.repository.CrudRepository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface FilmRepository extends CrudRepository<Film, UUID> {
  <S extends Film> Iterable<S> saveAllMergeIfExists(Iterable<S> entities);

  Optional<Film> findBySenderAndTitelIgnoreCaseAndThemaIgnoreCaseAndDuration(
      Sender sender, String titel, String thema, Duration duration);
}
