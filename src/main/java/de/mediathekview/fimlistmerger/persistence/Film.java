package de.mediathekview.fimlistmerger.persistence;

import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
  @OneToMany(mappedBy = "film", fetch = FetchType.EAGER)
  @Where(clause = "type='FILM_URL'")
  private Set<de.mediathekview.fimlistmerger.persistence.FilmUrl> urls;

  @Id
  @Column
  @Builder.Default
  private UUID uuid = UUID.randomUUID();

  @Column private LocalDateTime time;

  @ElementCollection(fetch = FetchType.EAGER) @Column private List<GeoLocations> geoLocations;

  @Column(columnDefinition = "TEXT") private String beschreibung;

  @Column private String website;

  @Column private boolean neu;

  @Column
  @OneToMany(mappedBy = "film", fetch = FetchType.EAGER)
  @Where(clause = "type='AUDIO_DESCRIPTION'")
  private Set<de.mediathekview.fimlistmerger.persistence.FilmUrl> audioDescriptions;

  @Column
  @OneToMany(mappedBy = "film", fetch = FetchType.EAGER)
  @Where(clause = "type='SIGN_LANGUAGE'")
  private Set<de.mediathekview.fimlistmerger.persistence.FilmUrl> signLanguages;

  @ElementCollection(fetch = FetchType.EAGER) @Column private Set<String> subtitles;

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

}
