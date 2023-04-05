package de.mediathekview.fimlistmerger.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import de.mediathekview.fimlistmerger.FilmPersistenceFilmMapper;
import de.mediathekview.fimlistmerger.FilmlistMergerApplication;
import de.mediathekview.fimlistmerger.FilmlistTestData;
import de.mediathekview.fimlistmerger.persistence.FilmUrl.Type;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("integration-test")
@ContextConfiguration(classes = FilmlistMergerApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayNameGeneration(ReplaceUnderscores.class)
class FilmRepositoryIT {

  @Inject FilmRepository filmRepository;
  @Inject FilmPersistenceService filmPersistenceService;

  @Test
  @DisplayName("Save all test films - three are saved")
  @Transactional
  void saveAll_newFilms_filmsSavedToDatabase() throws MalformedURLException {
    // WHEN
    filmPersistenceService.saveAllMergeIfExists(
        FilmlistTestData.createFilme().stream()
            .map(
                film ->
                    Mappers.getMapper(FilmPersistenceFilmMapper.class).filmToPersistenceFilm(film))
            .collect(Collectors.toSet()));

    // THEN
    assertThat(filmRepository.count()).isEqualTo(3);
  }

  @Test
  @Transactional
  void save_film_urls_are_saved()throws ExecutionException, InterruptedException {
    // GIVEN
    Film filmToSave =
            Film.builder()
                    .sender(Sender.ARTE_DE)
                    .thema("FilmRepositoryIT")
                    .titel("save_newFilm_filmSavedToDatabase")
                    .beschreibung("Save a new film to database check if it's saved")
                    .neu(true)
                    .time(LocalDateTime.now())
                    .duration(Duration.ofMinutes(45))
                    .urls(Set.of(
                            new FilmUrl(Type.FILM_URL, Resolution.NORMAL, "http://example.org/Test.mp4", 2L),
                            new FilmUrl(Type.FILM_URL, Resolution.HD, "http://example.org/hd.mp4", 2L),
                            new FilmUrl(Type.FILM_URL, Resolution.SMALL, "http://example.org/klein.mp4", 2L)
                    ))
                    .build();
    filmToSave.getUrls().forEach(filmUrl -> filmUrl.setFilm(filmToSave));

    // WHEN
    var savedFilm = filmPersistenceService.saveMergeIfExists(filmToSave);

    // THEN
    assertThat(filmRepository.findById(savedFilm.getUuid())).get().isEqualTo(filmToSave);
  }

  @Test
  @DisplayName("Save a new film to database check if it's saved")
  @Transactional
  void save_newFilm_filmSavedToDatabase() {
    // WHEN
    Film filmToSave =
        Film.builder()
            .sender(Sender.ARTE_DE)
            .thema("FilmRepositoryIT")
            .titel("save_newFilm_filmSavedToDatabase")
            .beschreibung("Save a new film to database check if it's saved")
            .neu(true)
            .time(LocalDateTime.now())
            .duration(Duration.ofMinutes(45))
            .build();
    filmPersistenceService.saveMergeIfExists(filmToSave);

    // THEN
    assertThat(filmRepository.count()).isEqualTo(1);
    assertThat(filmRepository.findById(filmToSave.getUuid())).isPresent();
  }
}
