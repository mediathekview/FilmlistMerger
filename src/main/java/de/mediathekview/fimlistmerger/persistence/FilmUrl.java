package de.mediathekview.fimlistmerger.persistence;

import de.mediathekview.fimlistmerger.FilmPersistenceFilmMapper;
import de.mediathekview.mlib.daten.Resolution;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FilmUrl {
  @Transient
  Logger LOG = LoggerFactory.getLogger(FilmUrl.class);
    
  @ManyToOne
  @JoinColumn(name = "filmId", referencedColumnName = "uuid")
  public Film film;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqGen")
  @SequenceGenerator(name = "seqGen", sequenceName = "film_urls_id_seq", allocationSize = 100)
  private long id;

  @Column(length = 400) private String url;

  @Column
  @Enumerated(EnumType.STRING)
  private Resolution resolution;

  @Column private Long fileSize;

  @Column
  @Enumerated(EnumType.STRING)
  private Type type;

  public FilmUrl(Type type, Resolution resolution, String url, Long fileSize) {
    super();
    this.type = type;
    this.url = url;
    this.resolution = resolution;
    this.fileSize = fileSize;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "("
        + "id = "
        + id
        + ", "
        + "url = "
        + url
        + ", "
        + "resolution = "
        + resolution
        + ", "
        + "fileSize = "
        + fileSize
        + ", "
        + "type = "
        + type
        + ")";
  }

  public enum Type {
    FILM_URL,
    AUDIO_DESCRIPTION,
    SIGN_LANGUAGE
  }

  @Override
  public int hashCode() {
    if (getFilm() == null) {
      //LOG.info("no Film object");
      return super.hashCode();  
    } else {
      String key = new String(getType() + getResolution().toString() + getFilm().getUuid());
      //LOG.info("film object " + new Integer(key.hashCode()).toString());
      return key.hashCode();
    }
  }

  @Override
  public boolean equals(Object obj) {
    // self check
    if (this == obj)
        return true;
    // null check
    if (obj == null)
        return false;
    // type check Class
    if (getClass() != obj.getClass())
        return false;
    // compare hashcode
    return this.hashCode() == obj.hashCode();
}


}
