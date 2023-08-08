package de.mediathekview.fimlistmerger;

public enum Metrics {
    COUNTER_READ_FILMS_OLD_FORMAT_MAX(Types.METRIC_TYPE_COUNTER,"filmlistmerger.films.read.format.old.max?increment=1&tags=app=filmlistmerger"),
    COUNTER_READ_FILMS_NEW_FORMAT_MAX(Types.METRIC_TYPE_COUNTER,"filmlistmerger.films.read.format.new.max?increment=1&tags=app=filmlistmerger"),
    COUNTER_FILMS_SAVED_CURRENT(Types.METRIC_TYPE_COUNTER,"filmlistmerger.films.saved.current?increment=1&tags=app=filmlistmerger"),
    SUMMARY_MERGED_FILMS_WRITE(Types.METRIC_TYPE_SUMMARY,"filmlistmerger.films.write.merged?tags=app=filmlistmerger"),
    TIMER_READ_FILMS_OLD_FORMAT_START(Types.METRIC_TYPE_TIMER,"filmlistmerger.films.read.format.old?action=start&tags=app=filmlistmerger"),
    TIMER_READ_FILMS_OLD_FORMAT_STOP(Types.METRIC_TYPE_TIMER,"filmlistmerger.films.read.format.old?action=stop&tags=app=filmlistmerger"),
    TIMER_READ_FILMS_NEW_FORMAT_START(Types.METRIC_TYPE_TIMER,"filmlistmerger.films.read.format.new?action=start&tags=app=filmlistmerger"),
    TIMER_READ_FILMS_NEW_FORMAT_STOP(Types.METRIC_TYPE_TIMER,"filmlistmerger.films.read.format.new?action=stop&tags=app=filmlistmerger"),
    //TIMER_WRITE_MERGED_FILMS_START(Types.METRIC_TYPE_TIMER,"filmlistmerger.films.write.merged?action=start&tags=app=filmlistmerger"),
    //TIMER_WRITE_MERGED_FILMS_STOP(Types.METRIC_TYPE_TIMER,"filmlistmerger.films.write.merged?action=stop&tags=app=filmlistmerger"),
    TIMER_WRITE_MERGED_FILMS_START(Types.METRIC_TYPE_TIMER,"filmlistmerger.films.writeTime.merged?action=start&tags=app=filmlistmerger"),
    TIMER_WRITE_MERGED_FILMS_STOP(Types.METRIC_TYPE_TIMER,"filmlistmerger.films.writeTime.merged?action=stop&tags=app=filmlistmerger"),
    TIMER_WRITE_FILM_START(Types.METRIC_TYPE_TIMER,"filmlistmerger.film.write?action=start&tags=app=filmlistmerger"),
    TIMER_WRITE_FILM_STOP(Types.METRIC_TYPE_TIMER,"filmlistmerger.film.write?action=stop&tags=app=filmlistmerger");

    private final String metricType;
    private final String metricName;

    Metrics(String metricType, String metricName) {
        this.metricType=metricType;
        this.metricName=metricName;
    }

    public String getMetricType() {
        return metricType;
    }

    public String getMetricName() {
        return metricName;
    }

    @Override
    public String toString() {
        return metricType+":"+metricName;
    }

    private static class Types {
        private static final String METRIC_TYPE_COUNTER = "micrometer:counter";
        private static final String METRIC_TYPE_SUMMARY = "micrometer:summary";
        private static final String METRIC_TYPE_TIMER = "micrometer:timer";
    }
}
