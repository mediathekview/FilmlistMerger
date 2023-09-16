package de.mediathekview.fimlistmerger.routes;

import static de.mediathekview.fimlistmerger.routes.ConvertOldFilmlistEntryToFilmRoute.DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE;
import static de.mediathekview.fimlistmerger.routes.ConvertOldFilmlistEntryToFilmRoute.ON_EXCEPTION_ID;
import static de.mediathekview.fimlistmerger.routes.ConvertOldFilmlistEntryToFilmRoute.ROUTE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.mediathekview.fimlistmerger.persistence.Film;
import de.mediathekview.fimlistmerger.persistence.FilmUrl;
import de.mediathekview.fimlistmerger.persistence.FilmUrl.Type;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    properties = {
      "camel.springboot.java-routes-include-pattern=**/" + ROUTE_ID + "*",
      "spring.liquibase.enabled=false"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableRouteCoverage
@DisplayNameGeneration(ReplaceUnderscores.class)
class ConvertOldFilmlistEntryToFilmRouteTest {

  @Autowired CamelContext camelContext;

  @EndpointInject("mock:direct:result")
  MockEndpoint mockEndpoint;

  @Produce("direct:producer")
  private ProducerTemplate template;

  @BeforeEach
  void setUp() throws Exception {
    setUpRouteUnderTest();
  }

  private void setUpRouteUnderTest() throws Exception {
    AdviceWith.adviceWith(
        camelContext,
        ROUTE_ID,
        advice -> {
          advice.weaveById(ON_EXCEPTION_ID).remove();
          advice.weaveByToUri(FilmToDatabaseTargetRoute.ROUTE_FROM).replace().to(mockEndpoint);
        });
  }

  @Test
  void small_entry_with_just_urls_gets_converted_to_film() throws InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(1);

    // when
    template.sendBody(
        DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE,
        new String[] {
          "ARD",
          "TestThema",
          "TestTitel",
          "01.01.2017",
          "23:55:00",
          "00:10:00",
          "2",
          "Test beschreibung.",
          "http://example.org/Test.mp4",
          "http://www.example.org/",
          "",
          "",
          "19|klein.mp4",
          "",
          "19|hd.mp4",
          "",
          "1483311300",
          "",
          "",
          "false"
        });

    // then
    mockEndpoint.assertIsSatisfied();
    final List<Film> receivedEntries =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Film.class))
            .toList();

    assertThat(receivedEntries)
        .hasSize(1)
        .element(0)
        .usingRecursiveComparison()
        .ignoringFieldsOfTypes(UUID.class)
        .ignoringCollectionOrder()
        .ignoringFields("urls.film")
        .isEqualTo(
            Film.builder()
                .sender(Sender.ARD)
                .thema("TestThema")
                .titel("TestTitel")
                .time(LocalDateTime.of(2017, 1, 1, 23, 55, 0))
                .duration(Duration.ofMinutes(10))
                .beschreibung("Test beschreibung.")
                .urls(
                    Set.of(
                        new FilmUrl(
                            Type.FILM_URL, Resolution.NORMAL, "http://example.org/Test.mp4", 2L),
                        new FilmUrl(Type.FILM_URL, Resolution.HD, "http://example.org/hd.mp4", 2L),
                        new FilmUrl(
                            Type.FILM_URL, Resolution.SMALL, "http://example.org/klein.mp4", 2L)))
                .website("http://www.example.org/")
                .geoLocations(List.of(GeoLocations.GEO_NONE))
                .subtitles(Collections.emptySet())
                .build());
  }

  @Test
  void invalid_normal_film_url_throws_exception() {
    // when & then
    assertThatThrownBy(
            () ->
                template.sendBody(
                    DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE,
                    new String[] {
                      "ARD",
                      "TestThema",
                      "TestTitel",
                      "01.01.2017",
                      "23:55:00",
                      "00:10:00",
                      "2",
                      "Test beschreibung.",
                      "invalid",
                      "http://www.example.org/",
                      "",
                      "",
                      "19|klein.mp4",
                      "",
                      "19|hd.mp4",
                      "",
                      "1483311300",
                      "",
                      "",
                      "false"
                    }))
        .isInstanceOf(CamelExecutionException.class)
        .cause()
        .isInstanceOf(MissingFilmDataException.class)
        .hasMessage("Can't create a valid film because \"invalid\" isn't valid for normal url");
  }

  @Test
  void empty_duration_is_not_included() throws InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(1);

    // when
    template.sendBody(
        DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE,
        new String[] {
          "ARD",
          "TestThema",
          "TestTitel",
          "01.01.2017",
          "23:55:00",
          "",
          "2",
          "Test beschreibung.",
          "http://example.org/Test.mp4",
          "http://www.example.org/",
          "",
          "",
          "19|klein.mp4",
          "",
          "19|hd.mp4",
          "",
          "1483311300",
          "",
          "",
          "false"
        });

    // then
    mockEndpoint.assertIsSatisfied();
    final List<Film> receivedEntries =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Film.class))
            .toList();

    assertThat(receivedEntries).hasSize(1).element(0).extracting(Film::getDuration).isNull();
  }

  @Test
  void invalid_duration_is_not_included() throws InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(1);

    // when
    template.sendBody(
        DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE,
        new String[] {
          "ARD",
          "TestThema",
          "TestTitel",
          "01.01.2017",
          "23:55:00",
          "invalid",
          "2",
          "Test beschreibung.",
          "http://example.org/Test.mp4",
          "http://www.example.org/",
          "",
          "",
          "19|klein.mp4",
          "",
          "19|hd.mp4",
          "",
          "1483311300",
          "",
          "",
          "false"
        });

    // then
    mockEndpoint.assertIsSatisfied();
    final List<Film> receivedEntries =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Film.class))
            .toList();

    assertThat(receivedEntries).hasSize(1).element(0).extracting(Film::getDuration).isNull();
  }

  @Test
  void invalid_alternative_film_url_not_included_in_result() throws InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(1);

    // when
    template.sendBody(
        DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE,
        new String[] {
          "ARD",
          "TestThema",
          "TestTitel",
          "01.01.2017",
          "23:55:00",
          "00:10:00",
          "2",
          "Test beschreibung.",
          "http://example.org/Test.mp4",
          "http://www.example.org/",
          "",
          "",
          "1|invalid",
          "",
          "19|hd.mp4",
          "",
          "1483311300",
          "",
          "",
          "false"
        });

    // then
    mockEndpoint.assertIsSatisfied();
    final List<Film> receivedEntries =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Film.class))
            .toList();

    assertThat(receivedEntries)
        .hasSize(1)
        .element(0)
        .extracting(Film::getUrls)
        .matches(urls -> urls.size() == 2);
  }

  @Test
  void alternative_film_url_to_many_parts_not_included_in_result() throws InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(1);

    // when
    template.sendBody(
        DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE,
        new String[] {
          "ARD",
          "TestThema",
          "TestTitel",
          "01.01.2017",
          "23:55:00",
          "00:10:00",
          "2",
          "Test beschreibung.",
          "http://example.org/Test.mp4",
          "http://www.example.org/",
          "",
          "",
          "19|in|valid",
          "",
          "19|hd.mp4",
          "",
          "1483311300",
          "",
          "",
          "false"
        });

    // then
    mockEndpoint.assertIsSatisfied();
    final List<Film> receivedEntries =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Film.class))
            .toList();

    assertThat(receivedEntries)
        .hasSize(1)
        .element(0)
        .extracting(Film::getUrls)
        .matches(urls -> urls.size() == 2);
  }

  @Test
  void file_size_not_a_number_not_included_in_result() throws InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(1);

    // when
    template.sendBody(
        DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE,
        new String[] {
          "ARD",
          "TestThema",
          "TestTitel",
          "01.01.2017",
          "23:55:00",
          "00:10:00",
          "invalid",
          "Test beschreibung.",
          "http://example.org/Test.mp4",
          "http://www.example.org/",
          "",
          "",
          "19|klein.mp4",
          "",
          "19|hd.mp4",
          "",
          "1483311300",
          "",
          "",
          "false"
        });

    // then
    mockEndpoint.assertIsSatisfied();
    final List<Film> receivedEntries =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Film.class))
            .toList();

    assertThat(receivedEntries)
        .hasSize(1)
        .element(0)
        .extracting(Film::getUrls)
        .extracting(urls -> urls.iterator().next())
        .matches(url -> url.getFileSize() == null);
  }

  @Test
  void website_not_a_url_not_included_in_result() throws InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(1);

    // when
    template.sendBody(
        DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE,
        new String[] {
          "ARD",
          "TestThema",
          "TestTitel",
          "01.01.2017",
          "23:55:00",
          "00:10:00",
          "2",
          "Test beschreibung.",
          "http://example.org/Test.mp4",
          "invalid",
          "",
          "",
          "19|klein.mp4",
          "",
          "19|hd.mp4",
          "",
          "1483311300",
          "",
          "",
          "false"
        });

    // then
    mockEndpoint.assertIsSatisfied();
    final List<Film> receivedEntries =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Film.class))
            .toList();

    assertThat(receivedEntries).hasSize(1).element(0).extracting(Film::getWebsite).isNull();
  }

  @Test
  void datetime_date_invalid_not_included_in_result() throws InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(1);

    // when
    template.sendBody(
        DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE,
        new String[] {
          "ARD",
          "TestThema",
          "TestTitel",
          "invalid",
          "23:55:00",
          "00:10:00",
          "2",
          "Test beschreibung.",
          "http://example.org/Test.mp4",
          "http://www.example.org/",
          "",
          "",
          "19|klein.mp4",
          "",
          "19|hd.mp4",
          "",
          "1483311300",
          "",
          "",
          "false"
        });

    // then
    mockEndpoint.assertIsSatisfied();
    final List<Film> receivedEntries =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Film.class))
            .toList();

    assertThat(receivedEntries).hasSize(1).element(0).extracting(Film::getTime).isNull();
  }

  @Test
  void datetime_time_invalid_not_included_in_result() throws InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(1);

    // when
    template.sendBody(
        DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE,
        new String[] {
          "ARD",
          "TestThema",
          "TestTitel",
          "01.01.2017",
          "invalid",
          "00:10:00",
          "2",
          "Test beschreibung.",
          "http://example.org/Test.mp4",
          "http://www.example.org/",
          "",
          "",
          "19|klein.mp4",
          "",
          "19|hd.mp4",
          "",
          "1483311300",
          "",
          "",
          "false"
        });

    // then
    mockEndpoint.assertIsSatisfied();
    final List<Film> receivedEntries =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Film.class))
            .toList();

    assertThat(receivedEntries).hasSize(1).element(0).extracting(Film::getTime).isNull();
  }

  @Test
  void full_entry_with_just_urls_gets_converted_to_film() throws InterruptedException {
    // given
    mockEndpoint.expectedMessageCount(1);

    // when
    template.sendBody(
        DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE,
        new String[] {
          "BR",
          "Test full entry",
          "A test with a full filled entry",
          "26.03.2023",
          "10:12:05",
          "02:31:15",
          "15",
          "A test with a full filled entry to check the convert route.",
          "https://example.org/TestNormal.mp4",
          "https://www.example.org/",
          "https://example.org/TestNormal.ttf",
          "https://www.example.org/ignoredNormal.rtmp",
          "20|Klein.mp4",
          "https://www.example.org/ignoredKlein.rtmp",
          "20|Hd.mp4",
          "https://www.example.org/ignoredHd.rtmp",
          "1679818325",
          "https://example.org/TestNormalOldIgnored.mp4",
          "eu",
          "false"
        });

    // then
    mockEndpoint.assertIsSatisfied();
    final List<Film> receivedEntries =
        mockEndpoint.getExchanges().stream()
            .map(Exchange::getIn)
            .map(message -> message.getBody(Film.class))
            .toList();

    assertThat(receivedEntries)
        .hasSize(1)
        .element(0)
        .usingRecursiveComparison()
        .ignoringFieldsOfTypes(UUID.class)
        .ignoringCollectionOrder()
        .ignoringFields("urls.film")
        .isEqualTo(
            Film.builder()
                .sender(Sender.BR)
                .thema("Test full entry")
                .titel("A test with a full filled entry")
                .time(LocalDateTime.of(2023, 3, 26, 10, 12, 5))
                .duration(Duration.ofHours(2).plusMinutes(31).plusSeconds(15))
                .beschreibung("A test with a full filled entry to check the convert route.")
                .urls(
                    Set.of(
                        new FilmUrl(
                            Type.FILM_URL, Resolution.SMALL, "https://example.org/Klein.mp4", 15L),
                        new FilmUrl(
                            Type.FILM_URL,
                            Resolution.NORMAL,
                            "https://example.org/TestNormal.mp4",
                            15L),
                        new FilmUrl(
                            Type.FILM_URL, Resolution.HD, "https://example.org/Hd.mp4", 15L)))
                .website("https://www.example.org/")
                .subtitles(Set.of("https://example.org/TestNormal.ttf"))
                .geoLocations(List.of(GeoLocations.GEO_EU))
                .neu(false)
                .build());
  }
}
