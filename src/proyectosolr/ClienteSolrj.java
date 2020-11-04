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

    //Le llegan 500 Documentos
    public void AddDoc(ArrayList<TipoDocumento> Documentos) throws SolrServerException, IOException {
        SolrInputDocument doc = new SolrInputDocument();
        
        //Para todos los documentos
        for (int i = 0; i < Documentos.size(); i++) {
            //Recorre el numero de pares Name-Value que tiene el documento
            for (int j = 0; j < Documentos.get(i).getNumFields(); j++) {
                ArrayList<String> pair = Documentos.get(i).getPair(j);
                doc.addField(pair.get(0), pair.get(1));
            }
            //Tras añadir todos los pares del documento lo añade al cliente Solr
            solr.add(doc);
            doc.clear();
        }

        solr.commit();
    }
}
