package proyectosolr;

import TipoDatos.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
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

    public void CreateTrec_top_file(ArrayList<TipoQuery> consultas) throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery();
        FileWriter trec = new FileWriter("trec_top_file.txt");

        for (int i = 0; i < consultas.size(); i++) {

            String[] split = consultas.get(i).getQuery().split("\\s");
            String CincoPalabras = "";
            for (int j = 0; j < 5; j++) {
                CincoPalabras += split[j] + " ";
            }
            System.out.println("QUERY: " + consultas.get(i).getId() + "--->" + CincoPalabras);

            query.setQuery("text:" + CincoPalabras);
            query.setFields("id,score");

            QueryResponse rsp = solr.query(query);
            SolrDocumentList docs = rsp.getResults();
            for (int j = 0; j < docs.size(); j++) {
                System.out.println((i + 1) + " Q0 "+ docs.get(j).get("id")+ " " + j + " " + docs.get(j).get("score") + " RaulG");
                trec.write((i + 1) + " Q0 " + docs.get(j).get("id") + " " + j + " " + docs.get(j).get("score") + " RaulG\n");
            }
            System.out.println("-----------------------------------------------");
        }
        trec.close();
    }

    public void Queries(ArrayList<TipoQuery> consultas) throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery();

        //query.addFilterQuery("cat:electronics");
        //query.setFields("id","price","merchant","cat","store");
        for (int i = 0; i < consultas.size(); i++) {

            String[] split = consultas.get(i).getQuery().split("\\s");
            String CincoPalabras = "";
            for (int j = 0; j < 5; j++) {
                CincoPalabras += split[j] + " ";
            }
            System.out.println("QUERY: " + consultas.get(i).getId() + "--->" + CincoPalabras);

            query.setQuery("text:" + CincoPalabras);

            QueryResponse rsp = solr.query(query);
            SolrDocumentList docs = rsp.getResults();
            for (int j = 0; j < docs.size(); ++j) {
                System.out.println(docs.get(j));
            }
            System.out.println("-----------------------------------------------");
        }
    }
}
