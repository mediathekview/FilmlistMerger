package de.mediathekview.fimlistmerger;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Filmlist;

import java.util.ArrayList;
import java.util.List;

public class FilmlistSplitterBean {
  @SuppressWarnings("unused")
  public List<Film> splitBody(Filmlist filmlist) {
    return new ArrayList<>(filmlist.getFilms().values());
  }
}
