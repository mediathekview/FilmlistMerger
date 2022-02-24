package de.mediathekview.fimlistmerger;

import liquibase.Liquibase;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.support.DatabaseStartupValidator;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.stream.Stream;

@SpringBootApplication
public class FilmlistMergerApplication {
    public static void main(String[] args) {
		SpringApplication.run(FilmlistMergerApplication.class, args);
	}

	@Bean
	public DatabaseStartupValidator databaseStartupValidator(DataSource dataSource) {
		var dsv = new DatabaseStartupValidator();
		dsv.setDataSource(dataSource);
		return dsv;
	}

	@Bean
	public static BeanFactoryPostProcessor dependsOnPostProcessor() {
		return bf -> {
			// Let beans that need the database depend on the DatabaseStartupValidator
			// like the JPA EntityManagerFactory or Flyway
			String[] liquibase = bf.getBeanNamesForType(Liquibase.class);
			Stream.of(liquibase)
					.map(bf::getBeanDefinition)
					.forEach(it -> it.setDependsOn("databaseStartupValidator"));

			String[] jpa = bf.getBeanNamesForType(EntityManagerFactory.class);
			Stream.of(jpa)
					.map(bf::getBeanDefinition)
					.forEach(it -> it.setDependsOn("databaseStartupValidator"));
		};
	}
}