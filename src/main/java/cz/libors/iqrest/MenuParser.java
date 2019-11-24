package cz.libors.iqrest;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuParser {

    private State state = State.NOTHING;
    private Locale locale = new Locale("cs", "CZ");
    private Pattern dayNamePattern = Pattern.compile("^.*? ([0-9.\\s]*)$");
    private Pattern pricePattern = Pattern.compile("^(.*?)([0-9]*\\s*kč)(.*)$");
    private boolean weakly = false;

    private Menu menu = new Menu();
    private Menu.MenuDay currentDay = null;
    private List<Menu.Meal> currentMeals;

    private String mealNameBuilder = "";

    public Menu parse(String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (StringUtils.isEmpty(line) || line.contains("alergen")) {
                continue;
            }
            String lowerLine = line.toLowerCase(locale);
            state.parse(lowerLine, this);
        }
        return menu;
    }

    private enum State implements LineParser {
        NOTHING {
            @Override
            public void parse(String line, MenuParser context) {
                if (line.startsWith("polední")) {
                    Matcher dayNameMatcher = context.dayNamePattern.matcher(line);
                    Assert.isTrue(dayNameMatcher.matches(), "Day pattern does not match: " + line);
                    String dayName = dayNameMatcher.group(1);
                    context.currentDay = new Menu.MenuDay(DayNameUtil.getNameFromParsedPdf(dayName));
                    context.menu.getDays().add(context.currentDay);
                    context.state = DAY;
                    context.weakly = false;
                }
            }
        },

        DAY {
            @Override
            public void parse(String line, MenuParser context) {
                if (line.startsWith("týdenní")) {
                    context.weakly = true;
                } else if (line.startsWith("lunch")) {
                    context.state = NOTHING;
                } else if (line.split(" ").length <=3) { // kategorie
                        context.currentMeals = new ArrayList<>();
                        if (context.weakly) {
                            context.currentDay.getWeekly().put(line, context.currentMeals);
                        } else {
                            context.currentDay.getDaily().put(line, context.currentMeals);
                        }
                } else {
                    context.mealNameBuilder += line.trim() + " ";
                    if (line.contains("kč")) {
                        Menu.Meal meal = context.createMeal(context.mealNameBuilder.trim());
                        context.currentMeals.add(meal);
                        context.mealNameBuilder = "";
                    }
                }
            }
        }

    }

    private Menu.Meal createMeal(String origName) {
        Matcher matcher = pricePattern.matcher(origName);
        Assert.isTrue(matcher.matches(), "price pattern does not match: " + origName);
        String name = matcher.group(1) + matcher.group(3);
        String price = matcher.group(2);
        return new Menu.Meal(name, price);
    }

    interface LineParser {
        void parse(String line, MenuParser context);
    }

}
