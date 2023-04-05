package de.mediathekview.fimlistmerger.dataformat.oldfilmlist;

/**
 * This enum represents the old filmlist entry array columns
 */
public enum OldFilmlistEntryArrayColumns {
    /**
     * The channel which broadcasted this film.
     */
    SENDER(0),

    /**
     * The topic of the film.
     */
    THEMA(1),

    /**
     * The film title.
     */
    TITEL(2),

    /**
     * The broadcasting date.
     */
    DATUM(3),

    /**
     * The broadcasting time.
     */
    ZEIT(4),

    /**
     * The duration of the film.
     */
    DAUER(5),

    /**
     * The size of the film in mega-bytes.
     */
    GROESSE_MB(6),

    /**
     * The film description.
     */
    BESCHREIBUNG(7),

    /**
     * The URL of the film in normal resolution.
     */
    URL(8),

    /**
     * A URL of the broadcasting mediathek page viewing this film.
     */
    WEBSITE(9),

    /**
     * A URL to the subtitles of this film.
     */
    URL_UNTERTITEL(10),

    /**
     * A URL to a RTMP file of this film.<br/>
     * <b>Deprecated & Ignored</b>
     */
    URL_RTMP(11),

    /**
     * A URL of the film in small resolution.
     */
    URL_KLEIN(12),

    /**
     * A URL to a RTMP file in small resolution of this film.<br/>
     * <b>Deprecated & Ignored</b>
     */
    URL_RTMP_KLEIN(13),

    /**
     * A URL of the film in HD resolution.
     */
    URL_HD(14),

    /**
     * A URL to a RTMP file in HD resolution of this film.<br/>
     * <b>Deprecated & Ignored</b>
     */
    URL_RTMP_HD(15),

    /**
     * A timestamp of the broadcasting date time.
     */
    DATUM_L(16),

  /**
   * An old URL of this film.<br/>
   * <b>Deprecated & Ignored</b>
   */
  URL_HISTORY(17),

    /**
     * The geo-blocking information of this film.
     * @see de.mediathekview.mlib.daten.GeoLocations
     */
    GEO(18),

    /**
     * A boolean which is true if this film is new. Only used in the client.
     */
    NEU(19);

  private final int columnIndex;

  OldFilmlistEntryArrayColumns(final int columnIndex) {
        this.columnIndex=columnIndex;
    }

    public int columnIndex() {
      return columnIndex;
    }
}
