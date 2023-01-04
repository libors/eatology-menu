package cz.libors.iqrest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class MenuDayFlags {
    private final String day;
    private final Map<String, Boolean> flags;

    @JsonCreator
    public MenuDayFlags(@JsonProperty("day") String day,
                        @JsonProperty("flags") Map<String, Boolean> flags) {
        this.day = day;
        this.flags = flags;
    }

    public String getDay() {
        return day;
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }
}
