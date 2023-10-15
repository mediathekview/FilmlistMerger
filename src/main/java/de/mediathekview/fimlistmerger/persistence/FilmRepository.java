package de.mediathekview.fimlistmerger.persistence;

import de.mediathekview.mlib.daten.Sender;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface FilmRepository extends CrudRepository<Film, UUID> {
  
  // OOM
  //EntityGraph(attributePaths = {"urls", "audioDescriptions", "signLanguages", "subtitles", "geoLocations" }, type=EntityGraphType.FETCH)
  @Transactional(readOnly = true)
  @EntityGraph(attributePaths = {"urls", "audioDescriptions", "signLanguages", "subtitles", "geoLocations" }, type=EntityGraphType.FETCH)
  List<Film> findByUuidNotNull();
  
  @EntityGraph(attributePaths = {"urls", "audioDescriptions", "signLanguages", "subtitles", "geoLocations" }, type=EntityGraphType.FETCH)
  List<Film> findBySender(Sender sender);
    
  Optional<Film> findBySenderAndTitelIgnoreCaseAndThemaIgnoreCaseAndDuration(
      Sender sender, String titel, String thema, Duration duration);
  
  @Query("select uuid from de.mediathekview.fimlistmerger.persistence.Film where sender = ?1 and titel = ?2 and thema = ?3 and duration = ?4")
  Optional<UUID> UuidBySenderAndTitelIgnoreCaseAndThemaIgnoreCaseAndDuration(
      Sender sender, String titel, String thema, Duration duration);
  
}
