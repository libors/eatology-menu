package cz.libors.iqrest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class Menu {

    private List<MenuDay> days = new ArrayList<>();

    public List<MenuDay> getDays() {
        return days;
    }

    public static class MenuDay {
        private String name;
        private Map<String, List<Meal>> daily = new LinkedHashMap<>();
        private Map<String, List<Meal>> weekly = new LinkedHashMap<>();

        @JsonCreator
        public MenuDay() {}

        public MenuDay(String name) {
            this.name = name;
        }

        public Map<String, List<Meal>> getDaily() {
            return daily;
        }

        public Map<String, List<Meal>> getWeekly() {
            return weekly;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDaily(Map<String, List<Meal>> daily) {
            this.daily = daily;
        }

        public void setWeekly(Map<String, List<Meal>> weekly) {
            this.weekly = weekly;
        }
    }

    public static class Meal {
        private String name;
        private String price;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<String> flags;

        @JsonCreator
        public Meal(@JsonProperty("name") String name,
                    @JsonProperty("price") String price,
                    @JsonProperty("flags") List<String> flags) {
            this.name = name;
            this.price = price;
            this.flags = flags;
        }

        public String getName() {
            return name;
        }

        public String getPrice() {
            return price;
        }

        public List<String> getFlags() {
            return flags;
        }

        public void setFlags(List<String> flags) {
            this.flags = flags;
        }
    }
}
