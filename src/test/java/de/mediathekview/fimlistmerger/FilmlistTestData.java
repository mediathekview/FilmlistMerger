package de.mediathekview.fimlistmerger;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("HttpUrlsUsage")
public final class FilmlistTestData {
  private FilmlistTestData() {
    super();
  }

  public static List<Film> createFilme() throws MalformedURLException {
    final List<Film> films = new ArrayList<>();

    final Film testFilm1 =
        new Film(
            UUID.fromString("c5c001cb-2fc2-494e-8723-cdf0726147be"),
            Sender.ARD,
            "TestTitel",
            "TestThema",
            LocalDateTime.parse("2017-01-01T23:55:00"),
            Duration.of(10, ChronoUnit.MINUTES));
    testFilm1.setWebsite(new URL("http://www.example.org/"));
    testFilm1.setBeschreibung("Test beschreibung.");
    testFilm1.addUrl(Resolution.SMALL, new FilmUrl(new URL("http://example.org/klein.mp4"), 42L));
    testFilm1.addUrl(Resolution.NORMAL, new FilmUrl(new URL("http://example.org/Test.mp4"), 42L));
    testFilm1.addUrl(Resolution.HD, new FilmUrl(new URL("http://example.org/hd.mp4"), 42L));

    final Film testFilm2 =
        new Film(
            UUID.fromString("fa75aeac-dee2-4820-8418-91e61ac586fe"),
            Sender.BR,
            "TestTitel",
            "TestThema2",
            LocalDateTime.parse("2018-01-01T23:55:00"),
            Duration.of(10, ChronoUnit.MINUTES));
    testFilm1.setWebsite(new URL("http://www.example.org/"));
    testFilm1.setBeschreibung("Test beschreibung.");
    testFilm1.addUrl(Resolution.SMALL, new FilmUrl(new URL("http://example.org/klein.mp4"), 42L));
    testFilm1.addUrl(Resolution.NORMAL, new FilmUrl(new URL("http://example.org/Test.mp4"), 42L));
    testFilm1.addUrl(Resolution.HD, new FilmUrl(new URL("http://example.org/hd.mp4"), 42L));

    final Film testFilm3 =
        new Film(
            UUID.fromString("4fd93b56-df3d-4182-ae18-04cf207e224e"),
            Sender.ARD,
            "TestTitel",
            "TestThema",
            LocalDateTime.parse("2017-01-01T23:55:00"),
            Duration.of(10, ChronoUnit.MINUTES));
    testFilm2.setWebsite(new URL("http://www.example.org/2"));
    testFilm2.setBeschreibung("Test beschreibung.");
    testFilm2.addUrl(Resolution.SMALL, new FilmUrl(new URL("http://example.org/klein2.mp4"), 42L));
    testFilm2.addUrl(Resolution.NORMAL, new FilmUrl(new URL("http://example.org/Test2.mp4"), 42L));
    testFilm2.addUrl(Resolution.HD, new FilmUrl(new URL("http://example.org/hd2.mp4"), 42L));

    films.add(testFilm1);
    films.add(testFilm2);
    films.add(testFilm3);
    return films;
  }
}
