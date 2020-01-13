package cz.libors.iqrest;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MenuParserTest {

    @Test
    public void testIt() throws IOException, TemplateException {
        InputStream is = new ClassPathResource("menu2.pdf").getInputStream();

        PDDocument pdDocument = PDDocument.load(is);

        PDFTextStripper textStripper = new PDFTextStripper();
        String text = textStripper.getText(pdDocument);

        //System.out.println(text);

        MenuParser parser = new MenuParser();
        Menu menu = parser.parse(text);

        Configuration cfg = new Configuration(new Version("2.3.28"));
        cfg.setClassForTemplateLoading(MenuParser.class, "/");
        cfg.setDefaultEncoding("UTF-8");

        for (Menu.MenuDay day : menu.getDays()) {
            Template template = cfg.getTemplate("freemarker/menu.ftl");
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("menu", day);
            templateData.put("admin", false);
            templateData.put("dayName", "day");
            StringWriter writer = new StringWriter();
            template.process(templateData, writer);
            System.out.println(writer.toString());
        }

    }

    @Test
    public void getFromUrl() throws IOException {
        URL website = new URL("http://www.iqrestaurant.cz/brno/menu.pdf");

        try (InputStream in = website.openStream()) {
            byte[] array = FileCopyUtils.copyToByteArray(in);
            System.out.println(array.length);
        }
    }

    @Test
    public void testDayNamePattern() {
        Pattern p = Pattern.compile("^.*? ([0-9. ]*)$");
        Matcher m = p.matcher("polední nabídka   pondělí 28. 1.");
        assertTrue(m.matches());
        assertEquals("28. 1.", m.group(1));
    }

    @Test
    public void dayOfWeek() {
        System.out.println(LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("cs", "CZ")));
    }


}