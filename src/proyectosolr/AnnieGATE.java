package proyectosolr;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.Out;
import gate.util.persistence.PersistenceManager;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import static proyectosolr.Comun.*;

public class AnnieGATE {

    private CorpusController annieController;

    private void initAnnie() throws GateException, IOException {
        Out.prln("Initialising ANNIE...");

        Gate.init();

        // load the ANNIE application from the saved state in plugins/ANNIE
        File pluginsHome = Gate.getPluginsHome();
        File anniePlugin = new File(pluginsHome, "ANNIE");
        File annieGapp = new File(anniePlugin, "ANNIE_with_defaults.gapp");
        annieController
                = (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);

        Out.prln("...ANNIE loaded");
    }

    private void createCorpus(String regex) throws ResourceInstantiationException, MalformedURLException, IOException {
        Corpus corpus = Factory.newCorpus("StandAloneAnnie corpus");

        ArrayList<String> DocsToString = new ArrayList<>();

        LeerFichero(DocsToString, regex);
        removeStopwords(DocsToString, regex);

        for (int i = 0; i < DocsToString.size(); i++) {
            FeatureMap params = Factory.newFeatureMap();
            params.put("stringContent", DocsToString.get(i));
            params.put("preserveOriginalContent", true);
            params.put("collectRepositioningInfo", true);
            //Out.prln("Creating doc for " + ArrayString.get(i));
            Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);
            corpus.add(doc);
        }

        annieController.setCorpus(corpus);
    }

    private ArrayList<String> execute() throws GateException, IOException {
        Out.prln("Running ANNIE...");
        annieController.execute();
        Out.prln("...ANNIE complete");

        ArrayList<String> result = new ArrayList<>();

        Iterator iter = annieController.getCorpus().iterator();

        while (iter.hasNext()) {
            Document doc = (Document) iter.next();
            AnnotationSet defaultAnnotSet = doc.getAnnotations();
            Set annotTypesRequired = new HashSet();
            annotTypesRequired.add("Identifier");
            annotTypesRequired.add("Location");
            annotTypesRequired.add("Organization");
            Set<Annotation> peopleAndPlaces
                    = new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));

            String xmlDocument = doc.toXml(peopleAndPlaces, false);
            result.add(xmlDocument);
            //System.out.println(xmlDocument);
        }
        return result;
    }

    public ArrayList<String> funciona(String regex) throws GateException, IOException {
        this.initAnnie();
        this.createCorpus(regex);
        return this.execute();
    }
}
