package proyectosolr;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

public class ClienteSolrj {

    HttpSolrClient solr;

    public ClienteSolrj() {
        solr = new HttpSolrClient.Builder("http://localhost:8983/solr/micoleccion").build();
    }

    public void AddDoc(ArrayList<String> fieldName,ArrayList<String> value) throws SolrServerException, IOException {
        SolrInputDocument doc = new SolrInputDocument();
        
        for (int i = 0; i < fieldName.size(); i++) {
            doc.addField(fieldName.get(i), value.get(i));
        }
        
        solr.add(doc);
        solr.commit();
    }
}
