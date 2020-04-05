package es.upm.dismanet.wikibot.bot;

import es.upm.dismanet.wikibot.model.Disease;
import net.sourceforge.jwbf.core.actions.HttpActionClient;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eduardo P. García del Valle
 */
public class WikiBot {

    public static final String DISNET_OMIM = "OMIM";
    public static final String DISNET_ICD10CM = "ICD-10";
    public static final String DISNET_ORPHANET = "Orphanet";
    public static final String DISNET_UMLS = "UMLS";
    public static final String DISNET_MESH = "MeSH";
    public static final String DISNET_SNOMEDCT = "SNOMED CT";
    public static final String DISNET_DO = "DO";
    public static final String DISNET_MESHSCR = "MeSHSCR";

    public static final String WIKIPEDIA_OMIM = "OMIM";
    public static final String WIKIPEDIA_ICD10 = "ICD10CM";
    public static final String WIKIPEDIA_ICD10CM = "ICD10CM";
    public static final String WIKIPEDIA_ORPHANET = "Orphanet";
    public static final String WIKIPEDIA_MESH = "MeshID";
    public static final String WIKIPEDIA_SNOMEDCT = "SNOMED CT";

    public static final Map<String, String> disnetToWikipediaResourceMap = new HashMap<String, String>() {{
        put(DISNET_OMIM, WIKIPEDIA_OMIM);
        put(DISNET_ICD10CM, WIKIPEDIA_ICD10CM);
        put(DISNET_ORPHANET, WIKIPEDIA_ORPHANET);
        put(DISNET_MESH, WIKIPEDIA_MESH);
        put(DISNET_SNOMEDCT, WIKIPEDIA_SNOMEDCT);
        put(DISNET_MESHSCR, WIKIPEDIA_MESH);
    }};

    private static final String wikiURL = "https://en.wikipedia.org/w/";
    private static final String wikiBotName = "DismanetBot";
    private static final String wikiBotVersion = "1.0";
    private static final String wikiBotComment = "https://en.wikipedia.org/wiki/User:Eduardo_P._Garc%C3%ADa_del_Valle";

    private static final String medicalResourceRegex = "\\|.\\s*(.*).*=\\s*(.*)";
    private static final Pattern pattern = Pattern.compile(medicalResourceRegex);

    private static final String wikiLineSep = "\n";

    private static final Logger logger = LogManager.getLogger(WikiBot.class);

    private final MediaWikiBot wikiBot;

    public WikiBot() {
        final HttpActionClient client = HttpActionClient.builder()
                .withUrl(wikiURL)
                .withUserAgent(wikiBotName, wikiBotVersion, wikiBotComment)
                .withRequestsPerUnit(10, TimeUnit.MINUTES)
                .build();

        wikiBot = new MediaWikiBot(client);
    }

    public void login(String userName, String password) {
        wikiBot.login(userName, password);
    }

    public void addMedicalResources(Disease disease) {
        final String wikiURL = disease.getDiseaseURL();
        final String wikiName = wikiURL.substring(wikiURL.lastIndexOf("/") + 1);

        logger.info("Processing disease '{}' with wikiURL '{}'", wikiName, wikiURL);

        final Article article = wikiBot.getArticle(wikiName);
        final String articleText = article.getText();
        final String newArticleText = replaceMedicalResources(articleText, disease.getDiseaseMappings());

        if (newArticleText.equals(articleText)) {
            logger.warn("Skip editing disease '{}'  with wikiURL '{}'", wikiName, wikiURL);

            return;
        }

        logger.info("Editing disease '{}' with wikiURL '{}'", wikiName, wikiURL);

        article.setText(newArticleText);
        article.setEditSummary("Adding new medical resources.");
        article.setMinorEdit(true);
        article.save();
    }

    protected String replaceMedicalResources(String articleText, Set<Disease.DiseaseMapping> diseaseMappings) {
        final Map<String, String> originalMedicalResourcesMap = getMedicalResourcesMap(articleText);

        if (originalMedicalResourcesMap.isEmpty()) {
            return articleText;
        }

        final Map<String, String> newMedicalResourceMap = getMedicalResourcesMap(originalMedicalResourcesMap, diseaseMappings);

        // Additional verification
        if (newMedicalResourceMap.equals(originalMedicalResourcesMap)) {
            return articleText;
        }

        return replaceMedicalResources(articleText, newMedicalResourceMap);
    }

    private Map<String, String> getMedicalResourcesMap(String text) {
        final String[] lines = text.split(wikiLineSep);

        boolean isExternalLinks = false;
        boolean isMedicalResources = false;

        final Map<String, String> medicalResourcesMap = new HashMap();

        for (String line : lines) {
            if (line.contains("== External links ==")) {
                isExternalLinks = true;

                continue;
            }

            if (line.contains("{{Medical") && isExternalLinks) {
                isMedicalResources = true;

                continue;
            }

            if (isMedicalResources && line.startsWith("}}")) {
                isMedicalResources = false;
                isExternalLinks = false;

                continue;
            }

            if (isMedicalResources) {
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    medicalResourcesMap.put(matcher.group(1).trim(), matcher.group(2).trim());
                }

            }
        }

        return medicalResourcesMap;
    }

    private Map<String, String> getMedicalResourcesMap(Map<String, String> medicalResourcesMap, Set<Disease.DiseaseMapping> diseaseMappings) {
        return new HashMap() {{
            diseaseMappings.forEach(dm -> {
                String wikiResource = disnetToWikipediaResourceMap.get(dm.getResource());

                if (dm.getResource().equals(DISNET_ICD10CM)) {
                    if (medicalResourcesMap.containsKey(wikiResource) && !(medicalResourcesMap.get(wikiResource).contains("<!--{{ICD10CM|Xxx.xxxx}}-->") || medicalResourcesMap.get(wikiResource).equals(""))) {
                        logger.debug("ICD10CM resource {} exists for disease {}", medicalResourcesMap.get(wikiResource), dm.getName());

                        return;
                    }

                    put(wikiResource, String.format("{{ICD10CM|%s}}", dm.getCode()));

                    return;
                }

                if (medicalResourcesMap.containsKey(wikiResource) && !medicalResourcesMap.get(wikiResource).equals("")) {
                    logger.debug("Resource {} exists for disease {}", medicalResourcesMap.get(wikiResource), dm.getName());

                    return;
                }

                put(wikiResource, dm.getCode());
            });
        }};
    }

    private String replaceMedicalResources(String text, Map<String, String> medicalResourcesMap) {
        final StringBuilder sb = new StringBuilder();

        final String[] lines = text.split(wikiLineSep);

        boolean isExternalLinks = false;
        boolean isMedicalResources = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.contains("== External links ==")) {
                addLine(sb, line);

                isExternalLinks = true;

                continue;
            }

            if (line.contains("{{Medical") && isExternalLinks) {
                addLine(sb, line);

                isMedicalResources = true;

                continue;
            }

            if (isMedicalResources && line.startsWith("}}")) {
                for (Map.Entry<String, String> entry : medicalResourcesMap.entrySet()) {
                    String wikiResource = entry.getKey();

                    sb.append("| " + entry.getKey());

                    for (int j = 0; j < (16 - wikiResource.length()); j++) {
                        sb.append(" ");
                    }

                    addLine(sb, "= " + medicalResourcesMap.get(wikiResource));
                }

                addLine(sb, line);

                isMedicalResources = false;
                isExternalLinks = false;

                continue;
            }

            if (isMedicalResources) {
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    String wikiResource = matcher.group(1).trim();

                    if (medicalResourcesMap.containsKey(wikiResource)) {
                        sb.append("| " + wikiResource);

                        for (int j = 0; j < (16 - wikiResource.length()); j++) {
                            sb.append(" ");
                        }

                        addLine(sb, "= " + medicalResourcesMap.get(wikiResource));
                        medicalResourcesMap.remove(wikiResource);
                    } else {
                        addLine(sb, line);
                    }
                }

                continue;
            }

            if (i == lines.length - 1) {
                sb.append(line);

                continue;
            }

            addLine(sb, line);
        }

        return sb.toString();
    }

    private void addLine(StringBuilder sb, String text) {
        sb.append(text + wikiLineSep);
    }

}
