package es.upm.dismanet.wikibot.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Eduardo P. García del Valle
 */
public class Disease {

    private String diseaseId;
    private String diseaseName;
    private String diseaseURL;
    private Set<DiseaseMapping> diseaseMappings = new HashSet();

    public Disease(String diseaseId, String diseaseName, String url) {
        this.diseaseId = diseaseId;
        this.diseaseName = diseaseName;
        this.diseaseURL = url;
    }

    public String getDiseaseId() {
        return diseaseId;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public Set<DiseaseMapping> getDiseaseMappings() {
        return diseaseMappings;
    }

    public void addMapping(String resource, String code, String name) {
        diseaseMappings.add(new DiseaseMapping(resource, code, name));
    }

    public String getDiseaseURL() {
        return diseaseURL;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        if (diseaseId != null)
            hash = 31 * hash + diseaseId.hashCode();

        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Disease)) {
            return false;
        }

        Disease d = (Disease) o;

        return d.getDiseaseId().equals(diseaseId);
    }

    public class DiseaseMapping {
        private String resource;
        private String code;
        private String name;

        private DiseaseMapping(String resource, String code, String name) {
            this.resource = resource;
            this.code = code;
            this.name = name;
        }

        public String getResource() {
            return resource;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            int hash = 7;

            if (resource != null)
                hash = 31 * hash + resource.hashCode();

            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof DiseaseMapping)) {
                return false;
            }

            DiseaseMapping m = (DiseaseMapping) o;

            return m.resource.equals(resource) && m.code.equals(code);
        }
    }
}
