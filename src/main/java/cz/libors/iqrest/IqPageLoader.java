package cz.libors.iqrest;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.libors.iqrest.Menu.MenuDay;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.*;

@Component
public class IqPageLoader {

    private final String rootPath;
    private final String iqurl;
    private final Template menuTemplate;
    private final Template missingTemplate;
    private final String password;

    private final ObjectMapper mapper = new ObjectMapper();

    public IqPageLoader(@Value("${filePath}") String rootPath,
                        @Value("${iqUrl}") String iqurl,
                        @Qualifier("menu") Template menuTemplate,
                        @Qualifier("missing") Template missingTemplate,
                        @Value("${password}") String password) {
        this.rootPath = rootPath;
        this.iqurl = iqurl;
        this.menuTemplate = menuTemplate;
        this.missingTemplate = missingTemplate;
        this.password = password;
    }

    public String loadMenuForName(String name, boolean admin) throws Exception {
        File file = Paths.get(rootPath, name).toFile();
        Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), UTF_8);
        MenuDay menu = mapper.readValue(reader, MenuDay.class);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("admin", admin);
        if (admin) {
            templateData.put("password", password);
        }
        templateData.put("menu", menu);
        templateData.put("dayName", DayNameUtil.dayOfWeek(menu.getName()));
        templateData.put("cssPath", "/iq/static/style.css");
        StringWriter stringWriter = new StringWriter();
        menuTemplate.process(templateData, stringWriter);
        return stringWriter.toString();
    }

    public String loadMissingMenuPage(String name, boolean admin) throws Exception {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("name", name);
        templateData.put("dayName", DayNameUtil.dayOfWeek(name));
        templateData.put("iqurl", iqurl);
        templateData.put("cssPath", "/iq/static/style.css");
        templateData.put("admin", admin);
        missingTemplate.process(templateData, stringWriter);
        return stringWriter.toString();
    }

}
