package de.mediathekview.fimlistmerger.routes;

import org.apache.camel.*;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static de.mediathekview.fimlistmerger.routes.SwitchOnFilmlistFormatRoute.ROUTE_ID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
    properties = {
      "camel.springboot.java-routes-include-pattern=**/" + ROUTE_ID + "*",
      "spring.liquibase.enabled=false"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableRouteCoverage
class SwitchOnFilmlistFormatRouteTest {
  @TempDir File tempDir;

  @Inject CamelContext camelContext;

  @EndpointInject("mock:direct:resultOld")
  MockEndpoint mockOldFormatEndpoint;

  @EndpointInject("mock:direct:resultNew")
  MockEndpoint mockNewFormatEndpoint;

  @EndpointInject("mock:direct:exception")
  MockEndpoint mockExceptionEndpoint;

  File tempInputFile;

  @Produce("direct:producer")
  private ProducerTemplate template;

  private String fromUri;

  @BeforeEach
  void setUp() throws Exception {
    tempInputFile = File.createTempFile("test", ".json", tempDir);
  }

  private void setUpRouteUnderTest() throws Exception {
    // GIVEN
    fromUri = "file://" + tempDir.getAbsolutePath();
    AdviceWith.adviceWith(
        camelContext,
        ROUTE_ID,
        advice -> {
          advice.replaceFromWith(fromUri);
          advice
              .weaveById(SwitchOnFilmlistFormatRoute.OLD_FILM_FORMAT_ROUTING_TARGET)
              .replace()
              .to(mockOldFormatEndpoint);
          advice
              .weaveById(SwitchOnFilmlistFormatRoute.NEW_FILM_FORMAT_ROUTING_TARGET)
              .replace()
              .to(mockNewFormatEndpoint);

          advice.weaveAddLast().to(mockExceptionEndpoint);
        });
  }

  @Test
  @DisplayName("Check if a filmlist with old format calls old filmlist format endpoint")
  void switchOnFilmlistFormat_oldFilmlist_OldFilmlistFormatEndpointCalled() throws Exception {
    setUpRouteUnderTest();
    // WHEN

    Files.copy(
        Paths.get(ClassLoader.getSystemResource("input/TestFilmlistOld.json").toURI()),
        tempInputFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING);

    // THEN
    mockNewFormatEndpoint.expectedMessageCount(0);
    mockNewFormatEndpoint.assertIsSatisfied();

    mockOldFormatEndpoint.expectedMessageCount(1);
    mockOldFormatEndpoint.assertIsSatisfied();
  }

  @Test
  @DisplayName("Check if a filmlist with new format calls new filmlist format endpoint")
  void switchOnFilmlistFormat_newFilmlist_NewFilmlistFormatEndpointCalled() throws Exception {
    setUpRouteUnderTest();
    // WHEN
    Files.copy(
        Paths.get(ClassLoader.getSystemResource("input/TestFilmlistNew.json").toURI()),
        tempInputFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING);

    // THEN
    mockOldFormatEndpoint.expectedMessageCount(0);
    mockOldFormatEndpoint.assertIsSatisfied();

    mockNewFormatEndpoint.expectedMessageCount(1);
    mockNewFormatEndpoint.assertIsSatisfied();
  }

  @Test
  @DisplayName("Check if a file which is not a filmlist leads to a exception")
  void switchOnFilmlistFormat_notFilmlist_ExceptionThrown() throws Exception {
    AdviceWith.adviceWith(
        camelContext, ROUTE_ID, advice -> advice.weaveAddLast().to(mockExceptionEndpoint));
    // GIVEN
    Files.copy(
        Paths.get(ClassLoader.getSystemResource("input/SimpleJsonFile.json").toURI()),
        tempInputFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING);

    // WHEN
    assertThatThrownBy(
            () ->
                template.sendBody(
                    SwitchOnFilmlistFormatRoute.DIRECT_SWITCH_ON_FILMLIST_FORMAT, tempInputFile))
        .isInstanceOf(CamelExecutionException.class)
        .hasStackTraceContaining("UnknownFilmlistFormatException");
    // THEN
    mockOldFormatEndpoint.expectedMessageCount(0);
    mockOldFormatEndpoint.assertIsSatisfied();

    mockNewFormatEndpoint.expectedMessageCount(0);
    mockNewFormatEndpoint.assertIsSatisfied();
  }
}
