package cz.libors.iqrest;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.libors.iqrest.Menu.MenuDay;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.*;

@Component
public class IqPageLoader {

    private String rootPath;
    private String iqurl;
    private Template menuTemplate;
    private Template missingTemplate;

    private ObjectMapper mapper = new ObjectMapper();

    public IqPageLoader(@Value("${filePath}") String rootPath,
                        @Value("${iqUrl}") String iqurl,
                        @Qualifier("menu") Template menuTemplate,
                        @Qualifier("missing") Template missingTemplate) {
        this.rootPath = rootPath;
        this.iqurl = iqurl;
        this.menuTemplate = menuTemplate;
        this.missingTemplate = missingTemplate;
    }

    public String loadMenuForName(String name) throws Exception {
        File file = Paths.get(rootPath, name).toFile();
        Reader reader = new InputStreamReader(new FileInputStream(file), UTF_8);
        MenuDay menu = mapper.readValue(reader, MenuDay.class);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("menu", menu);
        templateData.put("dayName", DayNameUtil.dayOfWeek(menu.getName()));
        StringWriter stringWriter = new StringWriter();
        menuTemplate.process(templateData, stringWriter);
        return stringWriter.toString();
    }

    public String loadMissingMenuPage(String name) throws Exception {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("name", name);
        templateData.put("dayName", DayNameUtil.dayOfWeek(name));
        templateData.put("iqurl", iqurl);
        missingTemplate.process(templateData, stringWriter);
        return stringWriter.toString();
    }

}
