package de.mediathekview.fimlistmerger.persistence;

import de.mediathekview.fimlistmerger.FilmlistMergerApplication;
import de.mediathekview.fimlistmerger.FilmlistTestData;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.net.MalformedURLException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = FilmlistMergerApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FilmRepositoryIT {

  @Inject FilmRepository filmRepository;

  @Test
  @Transactional
  void saveAll_newFilms_filmsSavedToDatabase() throws MalformedURLException {
    // WHEN
    filmRepository.saveAllMergeIfExists(
        FilmlistTestData.createFilme().stream().map(Film::new).collect(Collectors.toSet()));

    // THEN
    assertThat(filmRepository.count()).isEqualTo(2);
  }
}
