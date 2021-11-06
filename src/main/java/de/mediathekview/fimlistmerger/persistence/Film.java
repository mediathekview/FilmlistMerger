package de.mediathekview.fimlistmerger.persistence;

import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import lombok.*;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@IdClass(FilmId.class)
public class Film implements Serializable {
  @Serial private static final long serialVersionUID = -2915269140843158848L;

  @EmbeddedId private FilmId filmId;

  private Map<Resolution, FilmUrl> urls = new EnumMap<>(Resolution.class);
  private UUID uuid; // Old: filmNr
  private Sender sender;
  private LocalDateTime time;
  private Collection<GeoLocations> geoLocations;
  private String beschreibung;
  private URL website;
  private boolean neu;
  private Map<Resolution, FilmUrl> audioDescriptions = new EnumMap<>(Resolution.class);
  private Map<Resolution, FilmUrl> signLanguages = new EnumMap<>(Resolution.class);
  private Set<URL> subtitles;
}
