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

    private String replaceBasico = "\r\n|\r|\n|\\s{2,}|&apos;|&amp;";
    
     private String replaceEtiquetas = "<Identifier>|</Identifier>|"
            + "<Location>|</Location>|"
            + "<Organization>|</Organization>|";
    
    private String replacePuntuacion = "\\,|\\.|\\-|\\:|\\;";
    
    

    public ProyectoSolr(ClienteSolrj sol) throws IOException {
        DocFilesToString = new ArrayList<>();
        QUEFilesToString = new ArrayList<>();
        solrj = sol;
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

                String cadenaId = result[j].replaceAll("\\s{2,}", "");
                auxDoc.addPair("id", cadenaId);

                String title = result[j + 1].replaceAll(replaceBasico, " ");
                title = title.replaceAll(replaceEtiquetas, "");
                auxDoc.addPair("title", title);

                String auxToken;
                String text = auxToken = result[j + 2].replaceAll(replaceBasico, " ");
                text = text.replaceAll(replaceEtiquetas, "");
                auxDoc.addPair("text", text);

                Set<String> ORG = new HashSet<>();
                Set<String> LOC = new HashSet<>();

                StringTokenizer textTokenizer = new StringTokenizer(auxToken);
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
                auxDoc.addPair("Organization", ORG.toString().replaceAll("\\[|\\]", ""));
                auxDoc.addPair("Location", LOC.toString().replaceAll("\\[|\\]", ""));

                System.out.println(cadenaId + "\n" + title + "\n" + text + "\n"
                        + "Organizations: " + ORG.toString().replaceAll("\\[|\\]", "") + "\n"
                        + "Location: " + LOC.toString().replaceAll("\\[|\\]", "") + "\n");

                TodosDocs.add(auxDoc);
            }

        }
        solrj.AddDoc(TodosDocs); //Indexar documento a solr
    }

    public void parseQUE() throws SolrServerException, IOException, GateException {
        AnnieGATE gate = new AnnieGATE();
        QUEFilesToString = gate.funciona(regexQUEfiles);

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
                String id = result[j].replaceAll("\r\n|\r|\n", "");
                id = id.replaceAll(replaceEtiquetas, "");

                String query = result[j + 1].replaceAll("\r\n|\r|\n", " ");
                query = query.replaceAll(replaceEtiquetas, "");

                auxQUE = new TipoQuery(id, query);
                TodasQUE.add(auxQUE);
            }
        }
        //Realiza las consultas
        solrj.Queries(TodasQUE);
    }

    public static void main(String[] args) throws IOException, SolrServerException, InterruptedException, GateException {
        ProyectoSolr pd = new ProyectoSolr(new ClienteSolrj());

        //pd.prueba();
        pd.parseDocs();
        //     Thread.sleep(300);
        //    pd.parseQUE();
    }
}
