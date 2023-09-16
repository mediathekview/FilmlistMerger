package de.mediathekview.fimlistmerger.routes;

import static de.mediathekview.fimlistmerger.routes.InputFilesRoute.ROUTE_ID;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.EnableRouteCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
      "camel.springboot.java-routes-include-pattern=**/" + ROUTE_ID + "*",
      "spring.liquibase.enabled=false"
    })
@EnableRouteCoverage
class InputFilesRouteTest {
  @TempDir File tempDir;

  @Autowired CamelContext camelContext;

  @EndpointInject("mock:direct:result")
  MockEndpoint mockEndpoint;

  File tempInputFile;

  @Produce("direct:producer")
  private ProducerTemplate template;

  @BeforeEach
  void setUp() throws Exception {
    setUpRouteUnderTest();
    tempInputFile = File.createTempFile("test", ".json", tempDir);
    Files.copy(
        Paths.get(ClassLoader.getSystemResource("input/TestFilmlistOld.json").toURI()),
        tempInputFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING);
  }

  private void setUpRouteUnderTest() throws Exception {
    AdviceWith.adviceWith(
        camelContext,
        ROUTE_ID,
        advice -> {
          advice.replaceFromWith("file://" + tempDir.getAbsolutePath());
          advice
              .weaveById(InputFilesRoute.SWITCH_ON_FILMLIST_FORMAT_ROUTING_TARGET)
              .replace()
              .to(mockEndpoint);
        });
  }

  @Test
  @DisplayName("Tests if a simple test file is found on start")
  void findExistingFiles_simpleTestFile_TestFileFound() throws InterruptedException {
    mockEndpoint.expectedMessageCount(1);
    mockEndpoint.assertIsSatisfied();
  }
}
