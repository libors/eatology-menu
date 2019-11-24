package cz.libors.iqrest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.*;

@Component
public class IqMenuSaver {

    private String url;
    private String rootPath;
    private ObjectMapper mapper = new ObjectMapper();

    private static Logger log = LoggerFactory.getLogger(IqMenuSaver.class);

    private long lastDownload = 0;

    @Autowired
    public IqMenuSaver(@Value("${iqUrl}") String url,
                       @Value("${filePath}") String rootPath) {
        this.url = url;
        this.rootPath = rootPath;
    }

    public void saveCurrentMenu() {
        long lastSaveGap = System.currentTimeMillis() - lastDownload;
        if (lastSaveGap < 300000) {
            log.info("Skipping downloading menu, last try {} seconds ago.", lastSaveGap);
        } else {
            InputStream pdf = downloadPdf();
            String text = extractTextFromPdf(pdf);
            MenuParser parser = new MenuParser();
            Menu menu = parser.parse(text);
            saveMenuToFiles(menu);
            lastDownload = System.currentTimeMillis();
        }
    }

    private void saveMenuToFiles(Menu menu) {
        for (Menu.MenuDay day : menu.getDays()) {
            File f = Paths.get(rootPath, day.getName()).toFile();
            log.info("Saving file {}.", f.getAbsoluteFile());
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(f), UTF_8)) {
                mapper.writeValue(writer, day);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String extractTextFromPdf(InputStream inputStream) {
        try {
            PDDocument pdDocument = PDDocument.load(inputStream);
            PDFTextStripper textStripper = new PDFTextStripper();
            String text = textStripper.getText(pdDocument);
            pdDocument.close();
            return text;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream downloadPdf() {
        try {
            URL website = new URL(url);
            try (InputStream in = website.openStream()) {
                byte[] array = FileCopyUtils.copyToByteArray(in);
                return new ByteArrayInputStream(array);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
