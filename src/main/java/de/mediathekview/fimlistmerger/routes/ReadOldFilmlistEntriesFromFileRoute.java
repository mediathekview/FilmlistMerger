package de.mediathekview.fimlistmerger.routes;

import static de.mediathekview.fimlistmerger.routes.ConvertOldFilmlistEntryToFilmRoute.DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE;

import de.mediathekview.fimlistmerger.Metrics;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ReadOldFilmlistEntriesFromFileRoute extends RouteBuilder {

    private static final String ENTRY_PATTERN = "(?!\\{?\"?\\w*\"\\s?:\\s*\\[\\s?)\"([^\"]|\")*\",?\\s?";
  private static final String ENTRY_SPLIT_PATTERN = "(?<!\\\\)\",\"";
    private static final String QUOTATION_MARK = "\"";
    private static final String QUOTATION_MARK_AT_LINE_END = "\"$";

    public static final String ROUTE_ID = "ReadOldFilmlistEntriesFromFileRoute";
    public static final String DIRECT_READ_OLD_FILMLIST_ENTRIES_FROM_FILE = "direct:readOldFilmlistEntriesFromFile";
  private static final String ENTRIES_SPLIT_PATTERN = "],\\s*\"X\":\\s*\\[";
    private static final int SENDER_FIELD_INDEX = 0;
    private static final int THEMA_FIELD_INDEX = 1;
    private static final int TITLE_FIELD_INDEX = 2;

    private String[] entryBefore = null;

    @Override
    public void configure() {
        from(DIRECT_READ_OLD_FILMLIST_ENTRIES_FROM_FILE)
                .routeId(ROUTE_ID)

                .log(LoggingLevel.INFO, "Beginning to read filmlist in old format")
                .to(Metrics.TIMER_READ_FILMS_OLD_FORMAT_START.toString())

                .log(LoggingLevel.INFO, "Splitting old filmlist into it's entries")
                .split(body().tokenize(ENTRIES_SPLIT_PATTERN)).streaming()
                // Skip the first line just contain meta information
                .filter(simple("${exchangeProperty.CamelSplitIndex} > 0"))
                .process(this::convertEntryLineToArray)
                /*
                The Sender (Position 0), Thema (Position 1), and Title (Position 2) can be empty if the entry before
                already contained the same value.
                 */
                .log(LoggingLevel.INFO, "Filling sender, thema and title with the one from the entry before if empty")
                .process(this::fillEmptyFieldsWithValuesFromEntryBefore)

                .to(DIRECT_CONVERT_OLD_FILMLIST_ENTRY_TO_FILM_ROUTE)
        ;
    }

    private void fillEmptyFieldsWithValuesFromEntryBefore(Exchange exchange) {
        var entry = exchange.getIn().getBody(String[].class);
        if (entryBefore != null) {
            if (entry[SENDER_FIELD_INDEX] == null || entry[SENDER_FIELD_INDEX].isEmpty()) {
                entry[SENDER_FIELD_INDEX] = entryBefore[SENDER_FIELD_INDEX];
            }
            if (entry[THEMA_FIELD_INDEX] == null || entry[THEMA_FIELD_INDEX].isEmpty()) {
                entry[THEMA_FIELD_INDEX] = entryBefore[THEMA_FIELD_INDEX];
            }
            if (entry[TITLE_FIELD_INDEX] == null || entry[TITLE_FIELD_INDEX].isEmpty()) {
                entry[TITLE_FIELD_INDEX] = entryBefore[TITLE_FIELD_INDEX];
            }
            exchange.getMessage().setBody(entry);
        }
        entryBefore = entry;
    }

    private void convertEntryLineToArray(Exchange exchange) {
        final Matcher entryMatcher = Pattern.compile(ENTRY_PATTERN)
                .matcher(exchange.getIn().getBody(String.class));
        if (entryMatcher.find()) {
            exchange.getMessage().setBody(entryMatcher.group()
                    .replaceFirst(QUOTATION_MARK, "")
                    .replaceAll(QUOTATION_MARK_AT_LINE_END, "")
                    .trim()
                    .split(ENTRY_SPLIT_PATTERN));
        }
    }
}
