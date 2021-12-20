package de.mediathekview.fimlistmerger;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Filmlist;

import java.util.HashSet;
import java.util.Set;

public class FilmlistSplitterBean {
  @SuppressWarnings("unused")
  public Set<Film> splitBody(Filmlist filmlist) {
    return new HashSet<>(filmlist.getFilms().values());
  }
}
