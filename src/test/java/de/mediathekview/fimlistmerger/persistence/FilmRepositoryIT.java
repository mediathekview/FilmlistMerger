package de.mediathekview.fimlistmerger.persistence;

import de.mediathekview.fimlistmerger.FilmlistMergerApplication;
import de.mediathekview.fimlistmerger.FilmlistTestData;
import de.mediathekview.mlib.daten.Sender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("integration-test")
@ContextConfiguration(classes = FilmlistMergerApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FilmRepositoryIT {

  @Inject FilmRepository filmRepository;

  @Test
  @DisplayName("Save all test films - two are saved")
  @Transactional
  void saveAll_newFilms_filmsSavedToDatabase() throws MalformedURLException {
    // WHEN
    filmRepository.saveAllMergeIfExists(
        FilmlistTestData.createFilme().stream().map(Film::new).collect(Collectors.toSet()));

    // THEN
    assertThat(filmRepository.count()).isEqualTo(2);
  }

  @Test
  @DisplayName("Save a new film to database check if it's saved")
  @Transactional
  void save_newFilm_filmSavedToDatabase() {
    // WHEN
    filmRepository.saveMergeIfExists(
        Film.builder()
            .sender(Sender.ARTE_DE)
            .thema("FilmRepositoryIT")
            .titel("save_newFilm_filmSavedToDatabase")
            .beschreibung("Save a new film to database check if it's saved")
            .neu(true)
            .time(LocalDateTime.now())
            .duration(Duration.ofMinutes(45))
            .build());

    // THEN
    assertThat(filmRepository.count()).isEqualTo(2);
  }
}
