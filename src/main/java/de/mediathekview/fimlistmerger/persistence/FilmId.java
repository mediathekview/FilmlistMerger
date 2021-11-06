package de.mediathekview.fimlistmerger.persistence;

import de.mediathekview.mlib.daten.Sender;
import org.hibernate.Hibernate;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;

@Embeddable
public record FilmId(@Enumerated(EnumType.STRING) Sender sender, String titel, String thema, Duration duration) implements Serializable {
    @Serial
    private static final long serialVersionUID = 3351903451261388690L;

    public FilmId() {
        this(null, "", "", null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        FilmId filmId = (FilmId) o;
        return sender != null && Objects.equals(sender, filmId.sender)
                && titel != null && Objects.equals(titel, filmId.titel)
                && thema != null && Objects.equals(thema, filmId.thema)
                && duration != null && Objects.equals(duration, filmId.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender,
                titel,
                thema,
                duration);
    }
}

