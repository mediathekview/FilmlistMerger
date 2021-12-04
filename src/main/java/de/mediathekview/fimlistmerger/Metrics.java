package de.mediathekview.fimlistmerger;

public enum Metrics {
    COUNTER_READ_FILMS_OLD_FORMAT(Types.METRIC_TYPE_COUNTER,"counter.films.oldformat"),
    COUNTER_READ_FILMS_NEW_FORMAT(Types.METRIC_TYPE_COUNTER,"counter.films.newformat"),
    COUNTER_FILMS_SAVED(Types.METRIC_TYPE_COUNTER,"counter.films.saved");

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
        private static final String METRIC_TYPE_COUNTER = "metrics:counter";
    }
}
