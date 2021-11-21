package de.mediathekview.fimlistmerger;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Mapper()
@SuppressWarnings("unused")
public interface FilmPersistenceFilmMapper {
  Logger LOG = LoggerFactory.getLogger(FilmPersistenceFilmMapper.class);
  FilmPersistenceFilmMapper INSTANCE = Mappers.getMapper(FilmPersistenceFilmMapper.class);

  @Mapping(target = "merge", ignore = true)
  @Mapping(source = "uuid", target = "uuid")
  @Mapping(source = "sender", target = "sender")
  @Mapping(source = "time", target = "time")
  @Mapping(source = "website", target = "website", qualifiedByName = "fromStringToUrl")
  @Mapping(
      source = "audioDescriptions",
      target = "audioDescriptions",
      qualifiedByName = "fromPersistenceFilmUrlSetToResolutionFilmUrlMap")
  @Mapping(
      source = "signLanguages",
      target = "signLanguages",
      qualifiedByName = "fromPersistenceFilmUrlSetToResolutionFilmUrlMap")
  @Mapping(
      source = "urls",
      target = "urls",
      qualifiedByName = "fromPersistenceFilmUrlSetToResolutionFilmUrlMap")
  @Mapping(source = "subtitles", target = "subtitles", qualifiedByName = "fromUrlTextsToUrls")
  Film persistenceFilmToFilm(de.mediathekview.fimlistmerger.persistence.Film persistenceFilm);

  @Mapping(
      source = "urls",
      target = "urls",
      qualifiedByName = "fromFilmUrlMapToPersistenceFilmUrls")
  @Mapping(
      source = "audioDescriptions",
      target = "audioDescriptions",
      qualifiedByName = "fromAudioDescriptionUrlMapToPersistenceFilmUrls")
  @Mapping(
      source = "signLanguages",
      target = "signLanguages",
      qualifiedByName = "fromSignLanguageUrlMapToPersistenceFilmUrls")
  @Mapping(source = "website", target = "website", qualifiedByName = "unwrapUrl")
  de.mediathekview.fimlistmerger.persistence.Film filmToPersistenceFilm(Film film);

  @Named("fromUrlTextsToUrls")
  default Set<URL> fromUrlTextsToUrls(Collection<String> urlTexts) {
    if (urlTexts == null) {
      return Collections.emptySet();
    }
    return urlTexts.stream()
        .map(this::fromStringToUrl)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Named("unwrapUrl")
  default String unwrapUrl(Optional<URL> optionalUrl) {
    return optionalUrl.map(URL::toString).orElse(null);
  }

  default Set<String> fromUrlsToStringSet(Collection<URL> urls) {
    if (urls == null) {
      return Collections.emptySet();
    }
    return urls.parallelStream().map(URL::toString).collect(Collectors.toSet());
  }

  @Named("fromStringToUrl")
  default URL fromStringToUrl(String url) {
    if (url == null) {
      return null;
    }
    try {
      return new URL(url);
    } catch (MalformedURLException malformedURLException) {
      LOG.error("Can't convert a persisted url to a URL", malformedURLException);
      return null;
    }
  }

  @Named("fromPersistenceFilmUrlSetToResolutionFilmUrlMap")
  default Map<Resolution, FilmUrl> fromPersistenceFilmUrlSetToResolutionFilmUrlMap(
      Set<de.mediathekview.fimlistmerger.persistence.FilmUrl> urls) {
    if (urls == null) {
      return Collections.emptyMap();
    }

    Map<Resolution, FilmUrl> filmUrlMap = new EnumMap<>(Resolution.class);
    for (var filmUrl : urls) {
      try {
        filmUrlMap.put(
            filmUrl.getResolution(), new FilmUrl(filmUrl.getUrl(), filmUrl.getFileSize()));
      } catch (MalformedURLException malformedURLException) {
        LOG.error(
            "Can't convert a persisted film url back to a FilmUrl because the URL is invalid! {}",
            filmUrl,
            malformedURLException);
      }
    }
    return filmUrlMap;
  }

  @Named("fromFilmUrlMapToPersistenceFilmUrls")
  default Set<de.mediathekview.fimlistmerger.persistence.FilmUrl>
      fromFilmUrlMapToPersistenceFilmUrls(
          Map<Resolution, de.mediathekview.mlib.daten.FilmUrl> map) {
    return fromUrlMapToPersistenceFilmUrls(
        de.mediathekview.fimlistmerger.persistence.FilmUrl.Type.FILM_URL, map);
  }

  @Named("fromAudioDescriptionUrlMapToPersistenceFilmUrls")
  default Set<de.mediathekview.fimlistmerger.persistence.FilmUrl>
      fromAudioDescriptionUrlMapToPersistenceFilmUrls(
          Map<Resolution, de.mediathekview.mlib.daten.FilmUrl> map) {
    return fromUrlMapToPersistenceFilmUrls(
        de.mediathekview.fimlistmerger.persistence.FilmUrl.Type.AUDIO_DESCRIPTION, map);
  }

  @Named("fromSignLanguageUrlMapToPersistenceFilmUrls")
  default Set<de.mediathekview.fimlistmerger.persistence.FilmUrl>
      fromSignLanguageUrlMapToPersistenceFilmUrls(
          Map<Resolution, de.mediathekview.mlib.daten.FilmUrl> map) {
    return fromUrlMapToPersistenceFilmUrls(
        de.mediathekview.fimlistmerger.persistence.FilmUrl.Type.SIGN_LANGUAGE, map);
  }

  default Set<de.mediathekview.fimlistmerger.persistence.FilmUrl> fromUrlMapToPersistenceFilmUrls(
      de.mediathekview.fimlistmerger.persistence.FilmUrl.Type type,
      Map<Resolution, de.mediathekview.mlib.daten.FilmUrl> map) {
    if (map == null) {
      return Collections.emptySet();
    }
    return map.entrySet().stream()
        .map(
            entry ->
                new de.mediathekview.fimlistmerger.persistence.FilmUrl(
                    type,
                    entry.getKey(),
                    entry.getValue().getUrl().toString(),
                    entry.getValue().getFileSize()))
        .collect(Collectors.toSet());
  }
}
