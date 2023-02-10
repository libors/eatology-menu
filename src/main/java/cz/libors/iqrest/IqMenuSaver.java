package cz.libors.iqrest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

import static cz.libors.iqrest.Menu.*;
import static java.nio.charset.StandardCharsets.*;

@Component
public class IqMenuSaver {

    private final String url;
    private final String rootPath;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(IqMenuSaver.class);

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
            saveMenuToPdf(pdf, menu);
            lastDownload = System.currentTimeMillis();
        }
    }

    public void updateDayFlags(MenuDayFlags menuDayFlags) {
        File file = Paths.get(rootPath, menuDayFlags.getDay()).toFile();
        MenuDay day = readMenuDay(file);
        createBackupIfNotExists(file);
        MenuDay updated = DayFlagsUpdater.update(day, menuDayFlags);
        writeMenuDay(updated, file);
    }

    public void regenerate(String day) {
        File file = Paths.get(rootPath, day).toFile();
        Assert.isTrue(file.exists(), "File " + file + " does not exist.");
        MenuDay updated = regenerateDay(day);
        createBackupIfNotExists(file);
        writeMenuDay(updated, file);
    }

    private void createBackupIfNotExists(File file) {
        File backup = new File(file.getAbsolutePath() + ".backup");
        if (!backup.exists()) {
            log.info("Making backup file {}.", backup.getAbsolutePath());
            try {
                FileCopyUtils.copy(file, backup);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private MenuDay regenerateDay(String day) {
        InputStream pdf = findPdfForDay(day);
        String text = extractTextFromPdf(pdf);
        MenuParser parser = new MenuParser();
        Menu menu = parser.parse(text);
        MenuDay menuDay = menu.getDays().stream().filter(d -> d.getName().equals(day)).findFirst().orElse(null);
        Assert.isTrue(menuDay != null, "Cannot find menu day " + day);
        return menuDay;
    }

    private InputStream findPdfForDay(String day) {
        File file = Paths.get(rootPath, day + ".pdf").toFile();
        String currentDay = day;
        while (!file.exists() && DayNameUtil.dayOfWeekNum(currentDay) != 7) {
            currentDay = DayNameUtil.previousDay(currentDay);
            file = Paths.get(rootPath, currentDay + ".pdf").toFile();
        }
        Assert.isTrue(file.exists(), "No pdf find for menu day " + day);
        try {
            return Files.newInputStream(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveMenuToFiles(Menu menu) {
        for (MenuDay day : menu.getDays()) {
            File f = Paths.get(rootPath, day.getName()).toFile();
            log.info("Saving file {}.", f.getAbsoluteFile());
            writeMenuDay(day, f);
        }
    }

    private MenuDay readMenuDay(File file) {
        try (Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), UTF_8)) {
            return mapper.readValue(reader, MenuDay.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeMenuDay(MenuDay menuDay, File f) {
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(f.toPath()), UTF_8)) {
            mapper.writeValue(writer, menuDay);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveMenuToPdf(InputStream inputStream, Menu menu) {
        try {
            inputStream.reset();
            File f = Paths.get(rootPath, menu.getDays().get(0).getName() + ".pdf").toFile();
            FileCopyUtils.copy(inputStream, Files.newOutputStream(f.toPath()));
        } catch (IOException e) {
            throw new RuntimeException("Cannot save menu pdf", e);
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
