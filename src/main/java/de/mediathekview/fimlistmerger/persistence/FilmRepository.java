package de.mediathekview.fimlistmerger.persistence;

import de.mediathekview.mlib.daten.Sender;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface FilmRepository extends CrudRepository<Film, UUID> {
  Optional<Film> findBySenderAndTitelIgnoreCaseAndThemaIgnoreCaseAndDuration(
      Sender sender, String titel, String thema, Duration duration);
}
