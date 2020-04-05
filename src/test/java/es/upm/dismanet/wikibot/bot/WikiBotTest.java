package es.upm.dismanet.wikibot.bot;

import es.upm.dismanet.wikibot.model.Disease;
import es.upm.dismanet.wikibot.util.DiseaseCSVUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eduardo P. García del Valle
 */
public class WikiBotTest {

    private final static String testResourcePath = "src/test/resources/";

    private Map<String, Disease> diseaseMap = new HashMap();

    @Before
    public void setup() {
        diseaseMap = DiseaseCSVUtil.getDiseaseMap(Paths.get(testResourcePath + "disease_mappings.tsv"));
    }

    @Test
    public void testReplaceMedicalResourcesWithICD10CMMapping() throws IOException {
        WikiBot wikiBot = new WikiBot();

        String articleText = Files.readString(Paths.get(testResourcePath + "disease1.txt"));
        String expectedNewArticleText = Files.readString(Paths.get(testResourcePath + "disease1_exp.txt"));

        Disease disease1 = diseaseMap.get("disease1");

        String newArticleText = wikiBot.replaceMedicalResources(articleText, disease1.getDiseaseMappings());

        Assert.assertEquals(expectedNewArticleText, newArticleText);
    }

    @Test
    public void testReplaceMedicalResourcesWithMapping() throws IOException {
        WikiBot wikiBot = new WikiBot();

        String articleText = Files.readString(Paths.get(testResourcePath + "disease2.txt"));
        String expectedNewArticleText = Files.readString(Paths.get(testResourcePath + "disease2_exp.txt"));

        Disease disease1 = diseaseMap.get("disease2");

        String newArticleText = wikiBot.replaceMedicalResources(articleText, disease1.getDiseaseMappings());

        Assert.assertEquals(expectedNewArticleText, newArticleText);
    }

    @Test
    public void testReplaceMedicalResourcesWithoutMappings() throws IOException {
        WikiBot wikiBot = new WikiBot();

        String articleText = Files.readString(Paths.get(testResourcePath + "disease1.txt"));

        String newArticleText = wikiBot.replaceMedicalResources(articleText, Collections.emptySet());

        Assert.assertEquals(articleText, newArticleText);
    }

}

