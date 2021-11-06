package de.mediathekview.fimlistmerger.persistence;

import org.springframework.data.repository.CrudRepository;

public interface FilmRepository extends CrudRepository<FilmDAO, FilmId> {}
