package de.mediathekview.fimlistmerger.persistence;

import de.mediathekview.mlib.daten.Resolution;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FilmUrl {
  @ManyToOne
  @JoinColumn(name = "filmId", referencedColumnName = "uuid")
  private Film film;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column private String url;

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
}
