package de.mediathekview.fimlistmerger.routes;

import static de.mediathekview.fimlistmerger.dataformat.oldfilmlist.OldFilmlistEntryArrayColumns.*;
import static java.time.format.FormatStyle.MEDIUM;

import de.mediathekview.fimlistmerger.Metrics;
import de.mediathekview.fimlistmerger.dataformat.oldfilmlist.OldFilmlistEntryArrayColumns;
import de.mediathekview.fimlistmerger.persistence.Film;
import de.mediathekview.fimlistmerger.persistence.FilmUrl;
import de.mediathekview.fimlistmerger.persistence.FilmUrl.Type;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This route converts a string array of the old filmlist to a {@link Film} object.<br>
 * <br>
 * The old filmlist is an invalid JSON file which contains a map of X to array for each entry. The
 * entry array structure is represented by the {@link OldFilmlistEntryArrayColumns} enum.
 */
@Component
public class ConvertOldFilmlistEntryToFilmRoute extends RouteBuilder {

  private static final Logger LOG =
      LoggerFactory.getLogger(ConvertOldFilmlistEntryToFilmRoute.class);

  public static final String ROUTE_ID = "ConvertOldFilmlistEntryToFilmRoute";
  public static final String DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE =
      "direct:convertOldFilmlistEntryToFilmRoute";
  private static final String URL_SPLITTERATOR = "\\|";
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofLocalizedDate(MEDIUM).withLocale(Locale.GERMANY);
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofLocalizedTime(MEDIUM).withLocale(Locale.GERMANY);
  public static final String ON_EXCEPTION_ID = "convertOldFilmlistEntryExceptionHandling";
  public static final String CHOICE_ID = "choice";

  @Override
  public void configure() {
    from(DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE)
        .routeId(ROUTE_ID)
        .onCompletion()
        .to(Metrics.TIMER_READ_FILMS_OLD_FORMAT_STOP.toString())
        .end()
        .log(LoggingLevel.DEBUG, "Converting old filmlist entry to film")
        .process(this::convert)
        .to(Metrics.COUNTER_READ_FILMS_OLD_FORMAT_MAX.toString())
        .onException(MissingFilmDataException.class)
        .id(ON_EXCEPTION_ID)
        .log(LoggingLevel.INFO, "${body} isn't a valid film.")
        .to(Metrics.COUNTER_INVALID_FILMS.toString())
        .handled(true)
        .end()
        .filter(body().isInstanceOf(Film.class))
        .log(LoggingLevel.DEBUG, "Saving a film")
        .to(FilmToDatabaseTargetRoute.ROUTE_FROM);
  }

  protected void convert(Exchange exchange) {
    var entry = exchange.getMessage().getBody(String[].class);

    var fileSizeNormal = parseFileSizeNormal(entry[GROESSE_MB.columnIndex()]);

    var film =
        Film.builder()
            .sender(parseSender(entry[SENDER.columnIndex()]))
            .thema(parseThema(entry[THEMA.columnIndex()]))
            .titel(parseTitle(entry[TITEL.columnIndex()]))
            .time(parseDateTime(entry[DATUM.columnIndex()], entry[ZEIT.columnIndex()]))
            .duration(parseDuration(entry[DAUER.columnIndex()]))
            .beschreibung(entry[BESCHREIBUNG.columnIndex()])
            .urls(
                parseUrls(
                    fileSizeNormal,
                    entry[URL.columnIndex()],
                    entry[URL_KLEIN.columnIndex()],
                    entry[URL_HD.columnIndex()]))
            .website(parseWebsite(entry[WEBSITE.columnIndex()]))
            .subtitles(parseSubtitle(entry[URL_UNTERTITEL.columnIndex()]))
            .geoLocations(parseGeoLocation(entry[GEO.columnIndex()]))
            .build();

    film.getUrls().forEach(url -> url.setFilm(film));
    film.getSignLanguages().forEach(url -> url.setFilm(film));
    film.getAudioDescriptions().forEach(url -> url.setFilm(film));

    exchange.getMessage().setBody(film);
  }

  private List<GeoLocations> parseGeoLocation(String geoLocationText) {
    return Collections.singletonList(GeoLocations.getFromDescription(geoLocationText));
  }

  private FilmUrl createNormalFilmUrl(Long fileSizeNormal, String normalUrl) {
    try {
      return new FilmUrl(
          Type.FILM_URL, Resolution.NORMAL, new URL(normalUrl).toString(), fileSizeNormal);
    } catch (MalformedURLException malformedURLException) {
      throw new MissingFilmDataException("normal url", normalUrl, malformedURLException);
    }
  }

  private Optional<FilmUrl> createAlternativeFilmUrl(
      Long fileSizeNormal, Resolution resolution, String urlText) {
    try {
      return Optional.of(
          new FilmUrl(Type.FILM_URL, resolution, new URL(urlText).toString(), fileSizeNormal));
    } catch (MalformedURLException malformedURLException) {
      LOG.debug("Invalid url.", malformedURLException);
    }
    return Optional.empty();
  }

  private Set<FilmUrl> parseUrls(
      Long fileSizeNormal, String normalUrl, String smallUrl, String hdUrl) {
    var urls = new HashSet<FilmUrl>();
    urls.add(createNormalFilmUrl(fileSizeNormal, normalUrl));

    uncompressUrl(normalUrl, smallUrl)
        .flatMap(url -> createAlternativeFilmUrl(fileSizeNormal, Resolution.SMALL, url))
        .ifPresent(urls::add);

    uncompressUrl(normalUrl, hdUrl)
        .flatMap(url -> createAlternativeFilmUrl(fileSizeNormal, Resolution.HD, url))
        .ifPresent(urls::add);

    return urls;
  }

  private Set<String> parseSubtitle(String subtitleUrl) {
    return StringUtils.isEmpty(subtitleUrl) ? Collections.emptySet() : Set.of(subtitleUrl);
  }

  /**
   * The old filmlist contains the not normal urls in a compressed way like <code>19|hd.mp4</code>.
   * <br>
   * To uncompress these urls again we need the normal url and the text of the compressed url
   * because the number is the number of chars to take from the normal url.<br>
   * With a normal url <code>http://example.org/Test.mp4</code> an url text of <code>19|hd.mp4
   * </code> gets to <code>http://example.org/hd.mp4</code>.
   *
   * @param urlNormal The normal url to uncompress the compressed url again.
   * @param compressedUrlText The url text of the url to uncompress.
   * @return The uncompressed url.
   */
  private Optional<String> uncompressUrl(final String urlNormal, final String compressedUrlText) {

    final String[] splittedUrlText = compressedUrlText.split(URL_SPLITTERATOR);
    if (splittedUrlText.length == 2) {
      final int lengthOfOld = Integer.parseInt(splittedUrlText[0]);

      return Optional.of(urlNormal.substring(0, lengthOfOld) + splittedUrlText[1]);
    }
    return Optional.empty();
  }

  private Long parseFileSizeNormal(String fileSizeInMB) {
    if (null == fileSizeInMB) {
      LOG.debug("Missing file size, setting to null.");
      return null;
    }

    try {
      return Long.parseLong(fileSizeInMB);
    } catch (NumberFormatException numberFormatException) {
      LOG.debug("Invalid file size, setting to null.", numberFormatException);
      return null;
    }
  }

  private String parseWebsite(String website) {
    if (null == website) {
      LOG.debug("Missing website, setting to null.");
      return null;
    }

    try {
      return new URL(website.trim()).toString();
    } catch (MalformedURLException malformedURLException) {
      LOG.debug("Invalid website Url, setting to null.", malformedURLException);
      return null;
    }
  }

  private Duration parseDuration(String durationText) {
    if (StringUtils.isEmpty(durationText)) {
      return null;
    }

    try {
      return Duration.between(LocalTime.MIDNIGHT, LocalTime.parse(durationText));
    } catch (DateTimeParseException dateTimeParseException) {
      LOG.debug("Invalid duration, setting to null.", dateTimeParseException);
      return null;
    }
  }

  private LocalDateTime parseDateTime(String dateText, String timeText) {
    if (null == dateText) {
      LOG.debug("Missing date, setting date time to null.");
      return null;
    }
    if (null == timeText) {
      LOG.debug("Missing time, setting date time to null.");
      return null;
    }

    LocalDate date;
    try {
      date = LocalDate.parse(dateText, DATE_FORMATTER);
    } catch (DateTimeParseException dateTimeParseException) {
      LOG.debug("Invalid date, setting date time to null.", dateTimeParseException);
      return null;
    }

    LocalTime time;
    try {
      time = LocalTime.parse(timeText, TIME_FORMATTER);
    } catch (DateTimeParseException dateTimeParseException) {
      LOG.debug("Invalid time, setting date time to null.", dateTimeParseException);
      return null;
    }

    return LocalDateTime.of(date, time);
  }

  private String parseThema(String thema) {
    if (null == thema) {
      throw new MissingFilmDataException("Thema");
    }
    return thema;
  }

  private String parseTitle(String title) {
    if (null == title) {
      throw new MissingFilmDataException("Titel");
    }
    return title;
  }

  private Sender parseSender(String senderText) {
    if (null == senderText) {
      throw new MissingFilmDataException("Sender");
    }
    return Sender.getSenderByName(senderText)
        .orElseThrow(() -> new MissingFilmDataException("Sender", senderText));
  }
}
