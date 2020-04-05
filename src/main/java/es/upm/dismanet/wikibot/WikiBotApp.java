package es.upm.dismanet.wikibot;

import es.upm.dismanet.wikibot.bot.WikiBot;
import es.upm.dismanet.wikibot.model.Disease;
import es.upm.dismanet.wikibot.util.DiseaseCSVUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.Map;


/**
 * @author Eduardo P. García del Valle
 */
public class WikiBotApp {

    private static final Logger logger = LogManager.getLogger(WikiBotApp.class);

    public static void main(String[] args) {
        if (args.length != 3) {
            logger.error("Usage: java WikiBotApp [username] [password] [file]");
        }

        WikiBot wikiBot = new WikiBot();

        wikiBot.login(args[0], args[1]);

        Map<String, Disease> diseaseMap = DiseaseCSVUtil.getDiseaseMap(Paths.get(args[2]));

        logger.info("Total diseases {}.", diseaseMap.size());

        diseaseMap.values().forEach(d -> wikiBot.addMedicalResources(d));
    }

}
