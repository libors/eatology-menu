package cz.libors.iqrest;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static cz.libors.iqrest.DayNameUtil.checkMenuSaveRelevant;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@RestController
public class IqrestApplication {

    @Value("${filePath}")
    private String rootPath;
    @Autowired
    private IqMenuSaver iqMenuSaver;
    @Autowired
    private IqPageLoader iqPageLoader;

    private static final Logger log = LoggerFactory.getLogger(IqrestApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(IqrestApplication.class, args);
    }

    @GetMapping(path = "/iq", produces = "text/html; charset=UTF-8")
    public String todayMenu() throws Exception {
        String dayName = DayNameUtil.getNameFromCurrentDate();
        return getHtmlMenuString(dayName, true, false);
    }

    @GetMapping(path = "/iqadmin", produces = "text/html; charset=UTF-8")
    public String todayMenuAdmin() throws Exception {
        String dayName = DayNameUtil.getNameFromCurrentDate();
        return getHtmlMenuString(dayName, true, true);
    }

    @GetMapping(path = "/iq/{menu}", produces = "text/html; charset=UTF-8")
    public String dayMenu(@PathVariable("menu") String dayReference) throws Exception {
        String name = DayNameUtil.resolveNameFromLink(dayReference);
        return getHtmlMenuString(name, true, false);
    }

    @GetMapping(path = "/iqadmin/{menu}", produces = "text/html; charset=UTF-8")
    public String dayMenuAdmin(@PathVariable("menu") String dayReference) throws Exception {
        String name = DayNameUtil.resolveNameFromLink(dayReference);
        return getHtmlMenuString(name, true, true);
    }

    @PostMapping(path = "/iqadmin/update", consumes = "application/json")
    public void updateMenuItem(@RequestBody MenuDayFlags menuDayFlags) {
        iqMenuSaver.updateDayFlags(menuDayFlags);
    }

    @PutMapping(path="/iqadmin/regenerate/{menu}")
    public void regenerate(@PathVariable("menu") String dayReference) {
        iqMenuSaver.regenerate(dayReference);
    }

    private String getHtmlMenuString(String dayName, boolean tryDownload, boolean admin) throws Exception {
        File file = Paths.get(rootPath, dayName).toFile();
        if (!file.exists() && tryDownload) {
            if (checkMenuSaveRelevant(dayName)) {
                iqMenuSaver.saveCurrentMenu();
            } else {
                log.info("No reason to try to save menu for " + dayName);
            }
        }
        if (!file.exists()) {
            return iqPageLoader.loadMissingMenuPage(dayName, admin);
        } else {
            return iqPageLoader.loadMenuForName(dayName, admin);
        }
    }

    @Bean
    @Qualifier("menu")
    public Template menuTemplate() throws IOException {
        return configuration().getTemplate("menu.ftl");
    }

    @Bean
    @Qualifier("missing")
    public Template missingTemplate() throws IOException {
        return configuration().getTemplate("missing.ftl");
    }

    @Bean
    public Configuration configuration() {
        Configuration cfg = new Configuration(new Version("2.3.28"));
        cfg.setClassForTemplateLoading(MenuParser.class, "/freemarker/");
        cfg.setDefaultEncoding("UTF-8");
        return cfg;
    }

}

