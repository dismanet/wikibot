package es.upm.dismanet.wikibot.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import es.upm.dismanet.wikibot.model.Disease;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eduardo P. García del Valle
 */
public class DiseaseCSVUtil {

    private static final Logger logger = LogManager.getLogger(DiseaseCSVUtil.class);

    public static Map<String, Disease> getDiseaseMap(Path path) {
        final Map<String, Disease> diseaseMap = new HashMap();

        try (
                Reader reader = Files.newBufferedReader(path);
                CSVReader csvReader = new CSVReader(reader, '\t', CSVWriter.NO_QUOTE_CHARACTER, 1);
        ) {

            String[] nextRecord;

            while ((nextRecord = csvReader.readNext()) != null) {
                if (nextRecord.length != 6) {
                    continue;
                }

                String diseaseId = nextRecord[0];

                Disease disease = diseaseMap.getOrDefault(diseaseId, new Disease(diseaseId, nextRecord[1], nextRecord[2]));

                disease.addMapping(nextRecord[3], nextRecord[4], nextRecord[5]);

                diseaseMap.put(disease.getDiseaseId(), disease);
            }

        } catch (Exception e) {
            logger.error(e);
        }

        return diseaseMap;
    }
}
