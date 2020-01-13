package cz.libors.iqrest;

import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.*;

import static cz.libors.iqrest.Menu.*;
import static java.util.Arrays.asList;

public class DayFlagsUpdater {

    private static Set<String> knownFlags = new HashSet<>(asList("vege", "hot", "glut"));

    public static MenuDay update(MenuDay day, MenuDayFlags menuDayFlags) {
        int idx = 0;
        Map<Integer, List<String>> newFlags = flags(menuDayFlags);
        for (List<Meal> category : day.getDaily().values()){
            for (Meal meal : category) {
                List<String> toUpdate = newFlags.get(idx++);
                if (toUpdate != null) {
                    meal.setFlags(toUpdate);
                } else {
                    meal.setFlags(Collections.emptyList());
                }
            }
        }
        for (List<Meal> category : day.getWeekly().values()){
            for (Meal meal : category) {
                List<String> toUpdate = newFlags.get(idx++);
                if (toUpdate != null) {
                    meal.setFlags(toUpdate);
                } else {
                    meal.setFlags(Collections.emptyList());
                }
            }
        }
        return day;
    }

    private static Map<Integer, List<String>> flags(MenuDayFlags flags) {
        MultiValueMap<Integer, String> map = new LinkedMultiValueMap<>();
        for (Map.Entry<String, Boolean> entry : flags.getFlags().entrySet()) {
            if (entry.getValue()) {
                String[] flagAndIndex = StringUtils.split(entry.getKey(), "-");
                Assert.isTrue(knownFlags.contains(flagAndIndex[0]), "Unknown flag: " + flagAndIndex[0]);
                map.add(Integer.valueOf(flagAndIndex[1]), flagAndIndex[0]);
            }
        }
        return map;
    }

}
