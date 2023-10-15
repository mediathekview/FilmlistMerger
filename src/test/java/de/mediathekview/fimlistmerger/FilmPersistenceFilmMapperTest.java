package de.mediathekview.fimlistmerger;

import de.mediathekview.fimlistmerger.persistence.FilmUrl.Type;
import de.mediathekview.mlib.daten.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class FilmPersistenceFilmMapperTest {

  private final FilmPersistenceFilmMapper filmPersistenceFilmMapper =
      Mappers.getMapper(FilmPersistenceFilmMapper.class);

  private final UUID testFilmUUID = UUID.randomUUID();
  private final LocalDateTime testFilmTime = LocalDateTime.now();

  private Film createTestFilm() throws MalformedURLException {
    Film testFilm =
        new Film(
            testFilmUUID,
            Sender.NDR,
            "createTestFilm",
            "FilmPersistenceFilmMapperTest",
            testFilmTime,
            Duration.ofMinutes(42));
    testFilm.setNeu(true);
    testFilm.setBeschreibung("A test film to test the film to persistence film mapper");

    Map<Resolution, FilmUrl> audioDescriptions = new EnumMap<>(Resolution.class);
    audioDescriptions.put(
        Resolution.HD, new FilmUrl("http://localhost/hdAudioDescription.xml", 10L));
    audioDescriptions.put(
        Resolution.NORMAL, new FilmUrl("http://localhost/normalAudioDescription.xml", 5L));
    testFilm.setAudioDescriptions(audioDescriptions);

    Map<Resolution, FilmUrl> signLanguages = new EnumMap<>(Resolution.class);
    signLanguages.put(Resolution.SMALL, new FilmUrl("http://localhost/smallSignLanguage.xml", 1L));
    signLanguages.put(Resolution.UHD, new FilmUrl("http://localhost/uhdSignLanguage.xml", 20L));
    testFilm.setSignLanguages(signLanguages);

    Map<Resolution, FilmUrl> filmUrls = new EnumMap<>(Resolution.class);
    filmUrls.put(Resolution.VERY_SMALL, new FilmUrl("http://localhost/verySmallFilmUrl.xml", 1L));
    filmUrls.put(Resolution.WQHD, new FilmUrl("http://localhost/wqhdFilmUrl.xml", 25L));
    testFilm.addAllUrls(filmUrls);

    testFilm.setGeoLocations(List.of(GeoLocations.GEO_DE, GeoLocations.GEO_CH));
    testFilm.setWebsite(new URL("https://localhost/index"));
    return testFilm;
  }

  private de.mediathekview.fimlistmerger.persistence.Film createTestPersistenceFilm() {
    return de.mediathekview.fimlistmerger.persistence.Film.builder()
        .uuid(testFilmUUID)
        .sender(Sender.NDR)
        .titel("createTestFilm")
        .thema("FilmPersistenceFilmMapperTest")
        .time(testFilmTime)
        .duration(Duration.ofMinutes(42))
        .beschreibung("A test film to test the film to persistence film mapper")
        .audioDescriptions(
            Set.of(
                new de.mediathekview.fimlistmerger.persistence.FilmUrl(
                    Type.AUDIO_DESCRIPTION,
                    Resolution.HD,
                    "http://localhost/hdAudioDescription.xml",
                    10L),
                new de.mediathekview.fimlistmerger.persistence.FilmUrl(
                    Type.AUDIO_DESCRIPTION,
                    Resolution.NORMAL,
                    "http://localhost/normalAudioDescription.xml",
                    5L)))
        .signLanguages(
            Set.of(
                new de.mediathekview.fimlistmerger.persistence.FilmUrl(
                    Type.SIGN_LANGUAGE,
                    Resolution.SMALL,
                    "http://localhost/smallSignLanguage.xml",
                    1L),
                new de.mediathekview.fimlistmerger.persistence.FilmUrl(
                    Type.SIGN_LANGUAGE,
                    Resolution.UHD,
                    "http://localhost/uhdSignLanguage.xml",
                    20L)))
        .urls(
            Set.of(
                new de.mediathekview.fimlistmerger.persistence.FilmUrl(
                    Type.FILM_URL,
                    Resolution.VERY_SMALL,
                    "http://localhost/verySmallFilmUrl.xml",
                    1L),
                new de.mediathekview.fimlistmerger.persistence.FilmUrl(
                    Type.FILM_URL, Resolution.WQHD, "http://localhost/wqhdFilmUrl.xml", 25L)))
        .geoLocations(List.of(GeoLocations.GEO_DE, GeoLocations.GEO_CH))
        .website("https://localhost/index")
        .build();
  }

  @Test
  void film_mapToPersistenceFilm_persistenceFilm() throws MalformedURLException {
    assertThat(filmPersistenceFilmMapper.filmToPersistenceFilm(createTestFilm()))
        .isEqualTo(createTestPersistenceFilm());
  }

  @Test
  void persistenceFilm_mapToFilm_film() throws MalformedURLException {
    assertThat(filmPersistenceFilmMapper.persistenceFilmToFilm(createTestPersistenceFilm()))
        .isEqualTo(createTestFilm());
  }
}
