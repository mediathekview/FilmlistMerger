package de.mediathekview.fimlistmerger.persistence;

import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Table(name = "Film", indexes = {
        @Index(name = "idx_film_uuid", columnList = "uuid")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uc_film_sender_titel_thema", columnNames = {"sender", "titel", "thema"})
})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class Film implements Serializable {
  @Serial private static final long serialVersionUID = -2915269140843158848L;

  @Enumerated(EnumType.STRING)
  private Sender sender;

  @Column(nullable = false)
   private String titel;

  @Column(nullable = false)
   private String thema;

  @Column(nullable = false)
   private Duration duration;

  @Column
  @OneToMany(mappedBy = "film")
  @Where(clause = "type='FILM_URL'")
  private Set<de.mediathekview.fimlistmerger.persistence.FilmUrl> urls;

  @Id
  @Column
  @Builder.Default
  private UUID uuid = UUID.randomUUID();

  @Column private LocalDateTime time;

  @ElementCollection @Column private List<GeoLocations> geoLocations;

  @Column private String beschreibung;

  @Column private String website;

  @Column private boolean neu;

  @Column
  @OneToMany(mappedBy = "film")
  @Where(clause = "type='AUDIO_DESCRIPTION'")
  private Set<de.mediathekview.fimlistmerger.persistence.FilmUrl> audioDescriptions;

  @Column
  @OneToMany(mappedBy = "film")
  @Where(clause = "type='SIGN_LANGUAGE'")
  private Set<de.mediathekview.fimlistmerger.persistence.FilmUrl> signLanguages;

  @ElementCollection @Column private Set<String> subtitles;

  public Film(de.mediathekview.mlib.daten.Film film) {
    this.urls = convertFilmUrlMapToPersistenceFilmUrls(FilmUrl.Type.FILM_URL, film.getUrls());
    this.uuid = film.getUuid();
    this.sender = film.getSender();
    this.titel=film.getTitel();
    this.thema=film.getThema();
    this.duration=film.getDuration();
    this.time = film.getTime();
    this.geoLocations = new ArrayList<>(film.getGeoLocations());
    this.beschreibung = film.getBeschreibung();
    this.website = film.getWebsite().map(URL::toString).orElse("");
    this.neu = film.isNeu();
    this.audioDescriptions =
        convertFilmUrlMapToPersistenceFilmUrls(
            FilmUrl.Type.AUDIO_DESCRIPTION, film.getAudioDescriptions());
    this.signLanguages =
        convertFilmUrlMapToPersistenceFilmUrls(FilmUrl.Type.SIGN_LANGUAGE, film.getSignLanguages());
    this.subtitles = film.getSubtitles().stream().map(URL::toString).collect(Collectors.toSet());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Film film)) return false;
    return getSender() == film.getSender()
        && Objects.equals(getTitel(), film.getTitel())
        && Objects.equals(getThema(), film.getThema())
        && Objects.equals(getDuration(), film.getDuration());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSender(), getTitel(), getThema(), getDuration());
  }

  private Set<FilmUrl> convertFilmUrlMapToPersistenceFilmUrls(
      FilmUrl.Type type, Map<Resolution, de.mediathekview.mlib.daten.FilmUrl> map) {
    return map.entrySet().stream()
        .map(
            entry ->
                new FilmUrl(
                    type,
                    entry.getKey(),
                    entry.getValue().getUrl().toString(),
                    entry.getValue().getFileSize()))
        .collect(Collectors.toSet());
  }
}
