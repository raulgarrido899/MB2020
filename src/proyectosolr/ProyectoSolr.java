package proyectosolr;

import TipoDatos.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.solr.client.solrj.SolrServerException;

public class ProyectoSolr {

    private ClienteSolrj solrj;

    //Atributos para Documentos
    //private final String regexDocFiles = "glob:**LISA0.0*";// Solo lee 1 fichcero
    private String regexDocFiles = "glob:**LISA[0-5]*";
    private final String regexParseDocs = "(?<=Document\\s{1,4}[0-9]{1,4})\r\n|\r\n\r\n|\r\n\\s*\r\n|\n\\*{44}\r\n";
    private ArrayList<String> DocFilesToString;

    //Atributos para Queries
    private final String regexQUEfiles = "glob:**LISA.QUE*";
    private final String regexParseQUE = "(?<=(?m)^[0-9]{1,2})\r\n|\\s#\r\n";
    private ArrayList<String> QUEFilesToString;


    //Atributos para NUM
    private final String regexNUMfiles = "glob:**LISARJ.NUM";
    private ArrayList<String> NUMFileToString;

    //Convierte en String todo lo que lea del fichero separado por el regex
    private void LeerFichero(ArrayList<String> ArrayString, String regex) throws IOException {
        //Rellena pathFichero con el path de cada fichero dado por la expresion regular
        FileSystem fs = FileSystems.getDefault();
        PathMatcher pm = fs.getPathMatcher(regex);

        //Lee todos los ficheros de la carpeta Coleccion Lisa 
        //Si el fichero coincide con la expresion regular se pasa el contenido a String para parsearlo
        File directorio = new File("./Coleccion LISA/");
        File[] arrayFicheros = directorio.listFiles();
        for (File FilePath : arrayFicheros) {
            if (pm.matches(FilePath.toPath())) {
                ArrayString.add(new String(Files.readAllBytes(FilePath.toPath())));
            }
        }
    }

    public ProyectoSolr(ClienteSolrj sol) throws IOException {
        DocFilesToString = new ArrayList<>();
        QUEFilesToString = new ArrayList<>();
        NUMFileToString = new ArrayList<>();
        solrj = sol;

        LeerFichero(DocFilesToString, regexDocFiles);
    }

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

    public void parseDocs() throws SolrServerException, IOException {
        String[] result;
        //Lista donde se van guardando los Documentos
        ArrayList<TipoDocumento> TodosDocs = new ArrayList<>();

        //Para cada fichero leido se parsea y se guarda en result
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

                String[] cadenaId = result[j].split("Document\\s{1,4}");
                auxDoc.addPair("id", cadenaId[1]);
                auxDoc.addPair("title", result[j + 1].replaceAll("\r\n|\r|\n", " "));
                auxDoc.addPair("text", result[j + 2].replaceAll("\r\n|\r|\n", " "));

                TodosDocs.add(auxDoc);
            }

        }
        //Indexa 500 Documentos de 1 fichero
        solrj.AddDoc(TodosDocs); //Indexar documento a solr

    }

    //Crea fichero trec_rel_file
    public void parseNUM() throws IOException {
        LeerFichero(NUMFileToString, regexNUMfiles);
        ArrayList<String> result = new ArrayList<>();

        String NUM = NUMFileToString.get(0);
        NUM = NUM.replaceAll("(?m)^\\s*", ""); //QUITA ESPACIOS INICIALES
        String[] split = NUM.split("\r\n");

        /*
            Si la linea empieza por el numero del documento que se busca 
            guarda la cadena ya finalizada, crea una nueva y añade 1 al documento a buscar
            
            Si la linea NO empieza por el numero del documento añade esa linea a la cadena actual
         */
        //Añade la primera cadena
        String CadenaActual = split[0];
        int NumeroActual = 2;
        for (int i = 1; i < split.length; i++) {
            if (split[i].startsWith(String.valueOf(NumeroActual) + " ")) {
                CadenaActual = CadenaActual.replaceAll("\r\n|\r|\n", "");
                result.add(CadenaActual);
                CadenaActual = "";
                NumeroActual++;
                CadenaActual += split[i];
            } else {
                CadenaActual += split[i];
            }
        }
        //Añade la ultima cadena
        CadenaActual = CadenaActual.replaceAll("\r\n|\r|\n", "");
        result.add(CadenaActual);
        
        //    Crea el fichero con el formato
        //    Consulta 0 documento relevante[0|1] 
        FileWriter trec = new FileWriter("trec_rel_file.txt");
        for (int i = 0; i < result.size(); i++) {
            //Tokeniza String con Formato Consulta Documento1 Documento2 DocumentoN
            StringTokenizer st = new StringTokenizer(result.get(i));

            //Primer Token es la String con el id de la Consulta
            String Consulta = st.nextToken();
            //Los demas token son los id de los documentos relacionados ordenados
            ArrayList<String> DocRelacionados = new ArrayList<>();
            while (st.hasMoreTokens()) {
                DocRelacionados.add(st.nextToken());
            }

            //6004 numero de documentos totales
            int DocRelacionadoActual = 0;
            for (int j = 1; j <= 6004; j++) {
                //El Documento que va a escribir está dentro de la lista de DocumentosRelacionados
                if (j == Integer.parseInt(DocRelacionados.get(DocRelacionadoActual))) {
                    trec.write(Consulta + " 0 " + j + " 1\n");
                    if (DocRelacionadoActual < DocRelacionados.size() - 1) {
                        DocRelacionadoActual++;
                    }
                } else {
                    trec.write(Consulta + " 0 " + j + " 0\n");
                }
            }
        }
        trec.close();
    }

    public static void main(String[] args) throws IOException, SolrServerException, InterruptedException {
        ProyectoSolr pd = new ProyectoSolr(new ClienteSolrj());

   //     pd.parseDocs();
   //     Thread.sleep(300);
   //     pd.parseQUE();
        pd.parseNUM();
    }

}
