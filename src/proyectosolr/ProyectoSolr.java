package proyectosolr;

import TipoDatos.*;
import gate.util.GateException;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.solr.client.solrj.SolrServerException;
import static proyectosolr.Comun.*;

public class ProyectoSolr {

    private ClienteSolrj solrj;

    private ArrayList<String> DocFilesToString;
    private ArrayList<String> QUEFilesToString;

    public ProyectoSolr(ClienteSolrj sol) throws IOException {
        DocFilesToString = new ArrayList<>();
        QUEFilesToString = new ArrayList<>();
        solrj = sol;
    }

    public void prueba() throws SolrServerException, IOException, GateException {
        AnnieGATE gate = new AnnieGATE();
        DocFilesToString = gate.funciona(regexDocFiles);

        String[] result;
        for (int i = 0; i < DocFilesToString.size(); i++) {
            result = DocFilesToString.get(i).split(regexParseDocs);
            for (int j = 0; j < result.length; j++) {
                System.out.println(result[j]);
                System.out.println("");
                if (j % 3 == 2) {
                    System.out.println("-------------------------");
                }

            }
        }
    }

    public void parseDocs() throws SolrServerException, IOException, GateException {
        AnnieGATE gate = new AnnieGATE();
        DocFilesToString = gate.funciona(regexDocFiles);

        //Lista donde se van guardando los Documentos
        ArrayList<TipoDocumento> TodosDocs = new ArrayList<>();

        //Para cada fichero leido se parsea y se guarda en result
        String[] result;
        for (int i = 0; i < DocFilesToString.size(); i++) {
            //Parsea 500 Documentos de 1 fichero
            result = DocFilesToString.get(i).split(regexParseDocs);

            TipoDocumento auxDoc;
            //Recorre 500 Documentos donde --->
            //result[0] -- Numero Documento
            //result[1] -- Titulo Documento
            //result[2] -- Texto Documento
            for (int j = 0; j < result.length - 1; j += 3) {
                auxDoc = new TipoDocumento();

                String[] cadenaId = result[j].split("<Organization>Document</Organization>\\s{1,4}");
                auxDoc.addPair("id", cadenaId[1]);

                String title = result[j + 1].replaceAll("\r\n|\r|\n", " ");
                title = title.replaceAll("<Date>|</Date>|"
                        + "<Identifier>|</Identifier>|"
                        + "<Location>|</Location>|"
                        + "<Organization>|</Organization>|"
                        + "<YearTemp>|</YearTemp>", "");

                auxDoc.addPair("title", title);

                String text = result[j + 2].replaceAll("\r\n|\r|\n", " ");
                text = text.replaceAll("<Date>|</Date>|"
                        + "<Identifier>|</Identifier>|"
                        + "<Location>|</Location>|"
                        + "<Organization>|</Organization>|"
                        + "<YearTemp>|</YearTemp>", "");

                auxDoc.addPair("text", text);

                TodosDocs.add(auxDoc);
            }

        }
        //Indexa 500 Documentos de 1 fichero
        solrj.AddDoc(TodosDocs); //Indexar documento a solr
        /*
        for (int i = 0; i < TodosDocs.size(); i++) {
            for (int j = 0; j < TodosDocs.get(i).getNumFields(); j++) {
                System.out.println(TodosDocs.get(i).getPair(j).get(0));
                System.out.println(TodosDocs.get(i).getPair(j).get(1));
                System.out.println("");
            }
            
            System.out.println("--------------------");
        }
         */
    }

    //WIP
    public void parseQUE() throws SolrServerException, IOException {
        LeerFichero(QUEFilesToString, regexQUEfiles);

        String[] result;
        //Lista donde se van guardando los Documentos
        ArrayList<TipoQuery> TodasQUE = new ArrayList<>();

        for (int i = 0; i < QUEFilesToString.size(); i++) {
            result = QUEFilesToString.get(i).split(regexParseQUE);

            TipoQuery auxQUE;
            //Recorre 35 queries -->
            //result[0] -- Numero query
            //result[1] -- Texto query
            for (int j = 0; j < result.length - 1; j += 2) {
                auxQUE = new TipoQuery(result[j].replaceAll("\r\n|\r|\n", ""),
                        result[j + 1].replaceAll("\r\n|\r|\n", " "));

                TodasQUE.add(auxQUE);
            }
        }
        //Realiza las consultas
        solrj.Queries(TodasQUE);
    }

    public static void main(String[] args) throws IOException, SolrServerException, InterruptedException, GateException {
        ProyectoSolr pd = new ProyectoSolr(new ClienteSolrj());

        pd.parseDocs();
        //     Thread.sleep(300);
        //     pd.parseQUE();
        //    pd.parseNUM();
    }
}
