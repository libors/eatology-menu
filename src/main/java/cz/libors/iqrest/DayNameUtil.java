package cz.libors.iqrest;

import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.*;

public class DayNameUtil {

    private static Pattern pdfDatePattern = Pattern.compile("^\\s*([0-9]*)\\.\\s*([0-9]*)\\.\\s*([0-9]*)\\s*$");
    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d.M.yyyy");
    private static Locale locale = new Locale("cs", "CZ");

    public static String getNameFromCurrentDate() {
        return LocalDate.now().format(dateFormat);
    }

    private static LocalDate getFromMenuName(String menuName) {
        return LocalDate.parse(menuName, dateFormat);
    }

    public static String getNameFromCurrentWeek() {
        return LocalDate.now().with(MONDAY).format(dateFormat);
    }

    public static String getNameFromParsedPdf(String origPdfName) {
        LocalDate date = LocalDate.now();
        Matcher matcher = pdfDatePattern.matcher(origPdfName);
        Assert.isTrue(matcher.matches(), "date '" + origPdfName + "' does not match.");
        String year = matcher.group(3).isEmpty() ? String.valueOf(LocalDate.now().getYear()) : matcher.group(3);
        return matcher.group(1) + "." + matcher.group(2) + "." + year;
    }

    public static String resolveNameFromLink(String menuName) {
        String[] parts = menuName.split("-");
        LocalDate localDate = LocalDate.parse(parts[0], dateFormat);
        LocalDate targetDate = parts[1].equals("next") ? localDate.plusDays(1) : localDate.minusDays(1);
        return targetDate.format(dateFormat);
    }

    public static String dayOfWeek(String name) {
        return LocalDate.parse(name, dateFormat).getDayOfWeek().getDisplayName(TextStyle.FULL, locale);
    }

    public static boolean checkMenuSaveRelevant(String menuName, LocalDate now) {
        LocalDate menuDate = getFromMenuName(menuName);
        if (menuDate.getDayOfWeek().equals(SATURDAY) || menuDate.getDayOfWeek().equals(SUNDAY)) {
            return false; // no menu for weekends
        }
        LocalDate weekStart = now.with(previousOrSame(MONDAY));
        if (menuDate.isBefore(weekStart)) {
            return false; // cannot get menu for previous week
        }
        LocalDate weekEnd = now.with(nextOrSame(SUNDAY));
        if (menuDate.isAfter(weekEnd)) {
            LocalDate nextWeekEnd = weekEnd.with((next(SUNDAY)));
            if (menuDate.isAfter(nextWeekEnd)) {
                return false; // cannot get menu two weeks ahead
            } else {
                // on weekend, there is possibility that menu for next week is available
                return now.getDayOfWeek().equals(SUNDAY) || now.getDayOfWeek().equals(SATURDAY);
            }
        }
        return true;
    }

    public static boolean checkMenuSaveRelevant(String menuName) {
        return checkMenuSaveRelevant(menuName, LocalDate.now());
    }
}
