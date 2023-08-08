package de.mediathekview.fimlistmerger.dataformat.oldfilmlist;

import de.mediathekview.mlib.daten.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import static java.lang.String.format;
import static java.time.format.FormatStyle.MEDIUM;


////////////////////////////////////////
//REMOVE THIS SOURCE AFTER MLIB FIX
////////////////////////////////////////


public class OldFilmlistEntryToFilmTask implements Callable<Film> {
  private static final Logger LOG = LogManager.getLogger(OldFilmlistEntryToFilmTask.class);
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofLocalizedDate(MEDIUM).withLocale(Locale.GERMANY);
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm[:ss]", Locale.GERMANY);
  private static final LocalDate DEFAULT_DATE = LocalDate.parse("01.01.1970", DATE_FORMATTER);
  private static final char GEO_SPLITTERATOR = '-';
  private static final String URL_SPLITTERATOR = "\\|";
  private static final String EXCEPTION_TEXT_CANT_BUILD_FILM = "Can't build a Film from splits.";

  private final List<String> entrySplits;
  private final Future<Film> entryBefore;

  OldFilmlistEntryToFilmTask(final List<String> aEntrySplits, final Future<Film> aFilmEntryBefore) {
    entrySplits = aEntrySplits.stream().map(String::trim).toList();
    entryBefore = aFilmEntryBefore;
  }

  @Override
  public Film call() throws Exception {
    try {
      final String senderText = entrySplits.get(1);
      final Sender sender = gatherSender(senderText);
      final String thema = gatherTextOrUseAlternativ(2, AbstractMediaResource::getThema);
      final String titel = gatherTextOrUseAlternativ(3, AbstractMediaResource::getTitel);

      if (sender == null) {
          LOG.warn(format("Can't gather a Sender for %s %s %s",sender, thema, titel));
          throw new Exception(
            format("Can't gather a Sender for the film \"%s\" - \"%s\".", thema, titel));
      }
      
      final LocalDate date = gatherDate(sender, thema, titel);
      final LocalTime time = gatherTime(sender, thema, titel);
      final Duration dauer = gatherDuration(sender, thema, titel);
      final long groesse = gatherGroesse(sender, thema, titel);

      final String beschreibung = entrySplits.get(8);

      // Ignoring RTMP because can't find any usage.
      // Ignoring Film URL History because can't find any usage.
      final Film film =
          new Film(
              UUID.randomUUID(),
              sender,
              titel,
              thema,
              date == null ? null : LocalDateTime.of(date, time),
              dauer);
      addGeoLocations(film);
      setWebsite(film);
      setNeu(film);
      film.setBeschreibung(beschreibung);
      addSubtitle(film);
      // Here we import also films without a download URL so the next entry can use the Sender,
      // Thema and Titel. After the import all films without download URLs will be removed.
      addUrls(groesse, film);
      
      return film;
    } catch (final Exception exception) {
        LOG.error(format("Can't build film %s",exception));
        throw new Exception(EXCEPTION_TEXT_CANT_BUILD_FILM, exception);
    } 
  }

  private void addUrls(final long groesse, final Film film) {
      final String urlNormalText = entrySplits.get(9).trim();
      Optional<URL> oprionalUrlNormal = Optional.empty();
      try {
        oprionalUrlNormal = Optional.of(new URL(StringEscapeUtils.unescapeJava(urlNormalText)));
      } catch (MalformedURLException e) {
          LOG.warn(format("MalformedURLException video url %s for %s %s %s %s", urlNormalText , film.getSender(), film.getThema(), film.getTitel(), e));
      }
      if (oprionalUrlNormal.isPresent()) {
        final URL urlNormal = oprionalUrlNormal.get();
        film.addUrl(Resolution.NORMAL, new FilmUrl(urlNormal, groesse));
        addAlternativUrl(groesse, film, urlNormal, 13, Resolution.SMALL);
        addAlternativUrl(groesse, film, urlNormal, 15, Resolution.HD);
      }
  }

  private void addAlternativUrl(
      final long groesse, final Film film, final URL urlNormal, final int i, final Resolution small) {
    final String urlTextKlein = entrySplits.get(i);
    if (!urlTextKlein.isEmpty()) {
      try {
        final FilmUrl urlKlein = urlTextToUri(urlNormal, groesse, urlTextKlein);
        if (urlKlein != null) {
          film.addUrl(small, urlKlein);
        }
      } catch (MalformedURLException e) {
          LOG.warn(format("MalformedURLException video url %s for %s %s %s %s", urlTextKlein , film.getSender(), film.getThema(), film.getTitel(), e));
      }
    }
  }

  private void setNeu(final Film film) {
    final String neu = entrySplits.get(20);
    if (StringUtils.isNotBlank(neu)) {
      film.setNeu(Boolean.parseBoolean(neu));
    }
  }

  private void setWebsite(final Film film) {
    Optional<URL> urlWebseite = Optional.empty();
    final String websiteUrlText = entrySplits.get(10).trim();
    try {
      urlWebseite = Optional.of(new URL(StringEscapeUtils.unescapeJava(websiteUrlText)));
    } catch (final MalformedURLException malformedURLException) {
      LOG.warn(format("The website URL \"%s\" can't be parsed for %s %s %s %s", websiteUrlText, film.getSender(), film.getThema(), film.getTitel(), malformedURLException));
    }
    film.setWebsite(urlWebseite.orElse(null));
  }
  
  private void addGeoLocations(final Film film) {
    final Collection<GeoLocations> geoLocations = readGeoLocations(entrySplits.get(19));
    film.addAllGeoLocations(geoLocations);
  }

  private void addSubtitle(final Film film) {
    final String urlTextUntertitel = entrySplits.get(11);
    try {
      if (!urlTextUntertitel.isEmpty()) {
        film.addSubtitle(new URL(urlTextUntertitel));
      }
    } catch (MalformedURLException exception) {
        LOG.warn(format("Invalid subtitle url %s for %s %s %s", urlTextUntertitel, film.getSender(), film.getThema(), film.getTitel()));
    }
  }

  private long gatherGroesse(final Sender sender, final String thema, final String titel) {
    final String groesseText = entrySplits.get(7);

    long groesse = 0L;
    if (StringUtils.isNotBlank(groesseText)) {
      try {
          groesse = Long.parseLong(groesseText);
      } catch (NumberFormatException  e) {
          LOG.warn(format("NumberFormatException Size on %s for %s %s %s %s", groesseText, sender.getName(), thema, titel, e));    
      }
    } else {
      LOG.debug(format("Film ohne Größe \"%s %s - %s\".", sender.getName(), thema, titel));
    }
    return groesse;
  }

  private Duration gatherDuration(final Sender sender, final String thema, final String titel) {
    final String durationText = entrySplits.get(6);
    Duration dauer = Duration.ZERO;
    if (StringUtils.isNotBlank(durationText)) {
      try {
          dauer = Duration.between(LocalTime.MIDNIGHT, LocalTime.parse(durationText));
      } catch (DateTimeException | ArithmeticException e ) {
          LOG.warn(format("DateTimeException duration of %s for %s %s %s %s", durationText, sender.getName(), thema, titel, e));
      }
    } else {
      LOG.debug(format("Film ohne Dauer \"%s %s - %s\".", sender.getName(), thema, titel));
    }
    return dauer;
  }

  private LocalTime gatherTime(final Sender sender, final String thema, final String titel) {
    LocalTime time = LocalTime.MIDNIGHT;
    final String timeText = entrySplits.get(5);
    if (StringUtils.isNotBlank(timeText)) {
      try {
        time = LocalTime.parse(timeText, TIME_FORMATTER);
      } catch (DateTimeParseException  e) {
        LOG.warn(format("DateTimeParseException TIME on %s for %s %s %s %s", timeText, sender.getName(), thema, titel, e));
      }
    }
    return time;
  }
  
  @Nullable
  private LocalDate gatherDate(final Sender sender, final String thema, final String titel) {
    final String dateText = entrySplits.get(4);
    LocalDate date = DEFAULT_DATE;
    if (StringUtils.isNotBlank(dateText)) {
      try {
        date = LocalDate.parse(dateText, DATE_FORMATTER);
      } catch (DateTimeParseException e) {
        LOG.warn(format("DateTimeParseException DATE on %s for %s %s %s %s", dateText, sender.getName(), thema, titel, e));    
      }
    } else {
      LOG.debug(format("Film ohne Datum \"%s %s - %s\".", sender.getName(), thema, titel));
    }
    return date;
  }

  @Nullable
  private String gatherTextOrUseAlternativ(final int i, final Function<Film, String> alternativ)
      throws ExecutionException, InterruptedException {
    String text = entrySplits.get(i);
    if (StringUtils.isBlank(text) && entryBefore != null) {
      text = alternativ.apply(entryBefore.get());
    }
    return text;
  }

  private Sender gatherSender(final String senderText)
      throws InterruptedException, java.util.concurrent.ExecutionException {
    final Sender sender;
    if (StringUtils.isBlank(senderText) && entryBefore != null) {
      sender = entryBefore.get().getSender();
    } else {
      sender = Sender.getSenderByName(senderText).orElse(null);
    }
    return sender;
  }



  private Collection<GeoLocations> readGeoLocations(final String aGeoText) {
    final Collection<GeoLocations> geoLocations = new ArrayList<>();

    final GeoLocations singleGeoLocation = GeoLocations.getFromDescription(aGeoText);
    if (singleGeoLocation == GeoLocations.GEO_NONE) {
      for (final String geoText : aGeoText.split(String.valueOf(GEO_SPLITTERATOR))) {
        final GeoLocations geoLocation = GeoLocations.getFromDescription(geoText);
        if (geoLocation != GeoLocations.GEO_NONE) {
          geoLocations.add(geoLocation);
        }
      }
    } else {
      geoLocations.add(singleGeoLocation);
    }

    return geoLocations;
  }

  private FilmUrl urlTextToUri(final URL aUrlNormal, final long aGroesse, final String aUrlText)
          throws MalformedURLException {
        FilmUrl filmUrl = null;

        final String[] splittedUrlText = aUrlText.split(URL_SPLITTERATOR);
        if (splittedUrlText.length == 2) {
          final int lengthOfOld = Integer.parseInt(splittedUrlText[0]);

          final String newUrl = aUrlNormal.toString().substring(0, lengthOfOld) + splittedUrlText[1];
          filmUrl = new FilmUrl(new URL(newUrl), aGroesse);
        }
        return filmUrl;
      }
}