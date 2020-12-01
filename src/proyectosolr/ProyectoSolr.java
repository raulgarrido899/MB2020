package proyectosolr;

import TipoDatos.*;
import gate.util.GateException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.solr.client.solrj.SolrServerException;
import static proyectosolr.Comun.*;

public class ProyectoSolr {

    private ClienteSolrj solrj;

    private ArrayList<String> DocFilesToString;
    private ArrayList<String> QUEFilesToString;

    private final String replaceBasico = "\r\n|\r|\n|\\s{2,}|&apos;|&amp;";

    private final String replaceEtiquetas = "<Identifier>|</Identifier>|"
            + "<Location>|</Location>|"
            + "<Organization>|</Organization>|";

    private final String replacePuntuacion = "\\,|\\.|\\-|\\:|\\;";

    public ProyectoSolr(ClienteSolrj sol) throws IOException {
        DocFilesToString = new ArrayList<>();
        QUEFilesToString = new ArrayList<>();
        solrj = sol;
    }

    /**
     *
     * @param text Texto del cual obtener los valores de las etiquetas
     * @return TipoDocumento con todas las etiquetas y sus valores
     */
    private TipoDocumento getEtiquetas(String text) {
        TipoDocumento auxDoc = new TipoDocumento();

        Set<String> ORG = new HashSet<>();
        Set<String> LOC = new HashSet<>();

        StringTokenizer textTokenizer = new StringTokenizer(text);
        while (textTokenizer.hasMoreTokens()) {
            String next = textTokenizer.nextToken();

            //Toda la etiqueta en una linea
            if (next.startsWith("<Organization>") && next.endsWith("</Organization>")) {
                ORG.add(next.replaceAll("<Organization>|</Organization>|" + replacePuntuacion, ""));
            } //Etiqueta en varias lineas
            else if (next.startsWith("<Organization>")) {
                String aux = next;
                while (!next.contains("</Organization>")) {
                    next = textTokenizer.nextToken();
                    aux += " " + next;
                }
                ORG.add(aux.replaceAll("<Organization>|</Organization>|" + replacePuntuacion, ""));
            }

            //Toda la etiqueta en una linea
            if (next.startsWith("<Location>") && next.endsWith("</Location>")) {
                LOC.add(next.replaceAll("<Location>|</Location>|" + replacePuntuacion, ""));
            } //Etiqueta en varias lineas
            else if (next.startsWith("<Location>")) {
                String aux = next;
                while (!next.contains("</Location>")) {
                    next = textTokenizer.nextToken();
                    aux += " " + next;
                }
                LOC.add(aux.replaceAll("<Location>|</Location>|" + replacePuntuacion, ""));
            }
        }
        if(!ORG.isEmpty())
            auxDoc.addPair("Organization", ORG.toString().replaceAll("\\[|\\]", ""));
        if(!LOC.isEmpty())
            auxDoc.addPair("Location", LOC.toString().replaceAll("\\[|\\]", ""));

        return auxDoc;
    }

    private ArrayList<TipoDocumento> parseDocs() throws IOException, GateException {
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

                String cadenaId = result[j].replaceAll("\\s{2,}", "");
                auxDoc.addPair("id", cadenaId);

                String title = result[j + 1].replaceAll(replaceBasico, " ");
                title = title.replaceAll(replaceEtiquetas, "");
                auxDoc.addPair("title", title);

                String auxEtiquetas;
                String text = auxEtiquetas = result[j + 2].replaceAll(replaceBasico, " ");
                text = text.replaceAll(replaceEtiquetas, "");
                auxDoc.addPair("text", text);

                //Añade los pares de las etiquetas Location y Organization
                auxDoc.addAllPairs(getEtiquetas(auxEtiquetas));

                TodosDocs.add(auxDoc);
            }
        }
        return TodosDocs;
    }

    private ArrayList<TipoDocumento> parseQUE() throws IOException, GateException {
        AnnieGATE gate = new AnnieGATE();
        QUEFilesToString = gate.funciona(regexQUEfiles);

        String[] result;
        //Lista donde se van guardando los Documentos
        ArrayList<TipoDocumento> TodasQUE = new ArrayList<>();

        for (int i = 0; i < QUEFilesToString.size(); i++) {
            result = QUEFilesToString.get(i).split(regexParseQUE);

            TipoDocumento auxQUE;
            //Recorre 35 queries -->
            //result[0] -- Numero query
            //result[1] -- Texto query
            for (int j = 0; j < result.length - 1; j += 2) {
                String id = result[j].replaceAll(replaceBasico, "");
                id = id.replaceAll(replaceEtiquetas, "");

                String auxEtiquetas;
                String query = auxEtiquetas = result[j + 1].replaceAll(replaceBasico, " ");
                query = query.replaceAll(replaceEtiquetas, "");

                auxQUE = new TipoDocumento();
                auxQUE.addPair("id", id);
                auxQUE.addPair("text", query);

                //Añade los pares de las etiquetas Location y Organization
                auxQUE.addAllPairs(getEtiquetas(auxEtiquetas));
                
                    
                for (int zzz = 0; zzz < auxQUE.getNumPairs(); zzz++) {
                    System.out.print(auxQUE.getPair(zzz).get(0) + ": " + auxQUE.getPair(zzz).get(1) + "\n");
                }
                System.out.println("\n");
                TodasQUE.add(auxQUE);
            }
        }
        return TodasQUE;
    }

    public void IndexaDocs() throws IOException, GateException, SolrServerException {
        solrj.AddDoc(parseDocs()); //Indexar documento a solr
    }

    public void solrQUE() throws IOException, GateException, SolrServerException {
        //Realiza las consultas
        solrj.Queries(parseQUE());
    }

    //Crea fichero trec_top_file.txt
    public void TopFile() throws IOException, GateException, SolrServerException {
        solrj.CreateTrec_top_file(parseQUE());
    }

    public static void main(String[] args) throws IOException, SolrServerException, InterruptedException, GateException {
        ProyectoSolr pd = new ProyectoSolr(new ClienteSolrj());
        
        //pd.solrQUE();
        //pd.TopFile();
    }
}
