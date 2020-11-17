package proyectosolr;

import TipoDatos.TipoQuery;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.solr.client.solrj.SolrServerException;

public class Comun {
    
    //public static final String regexDocFiles = "glob:**LISA0.0*";// Solo lee 1 fichcero
    public static final String regexDocFiles = "glob:**LISA[0-5]*";
    public static final String regexParseDocs = "(?<=(?m)^\\s{1,4}[0-9]{1,4})\r\n|"
            + "\r\n\r\n|"
            + "\r\n\\s*\r\n|"
            + "\n\\*{44}\r\n";
    
    public static final String regexQUEfiles = "glob:**LISA.QUE*";
    public static final String regexParseQUE = "(?<=(?m)^[0-9]{1,2})\r\n|"
            + "\\s#\r\n";
    
    public static final String regexNUMfiles = "glob:**LISARJ.NUM";
    
    public static final ArrayList<String> DOCStopwords = new ArrayList<>(Arrays.asList(
            "THE", "OF", "AND", "IN", "TO", "A", "FOR", "Document", "INFORMATION", "IS", "ON", "ARE", "BY", "WITH", "AS",
            "BE", "AN", "AT", "THAT", "FROM", "WHICH", "PUBLIC", "WAS", "THEIR", "USE", "NEW",
            "HAS", "THIS", "IT", "ITS", "HAVE", "NOT", "DESCRIBES",
            "SOME", "OR", "WERE", "REPORT", "OTHER", "DISCUSSES", "BEEN", "SYSTEMS", "CAN", "WILL", "ALL",
            "WORK", "STUDY", "THESE", "SHOULD", "MORE", "SUCH", "ALSO", "REFERENCE",
            "BETWEEN", "STATE", "SCHOOL", "BUT", "SPECIAL", "BOOKS", "USED",
            "SUBJECT", "INTO", "THEY", "STAFF", "COLLECTION", "ABOUT", "PRESENTED", "GENERAL",
            "ROLE", "GIVEN", "USER", "FUTURE", "MOST", "RETRIEVAL", "NEED", "HOW", "THERE", "MUST", "RESULTS",
            "EACH", "THROUGH", "MADE", "PROVIDE", "NUMBER", "CURRENT", "SURVEY",
            "BASED", "US", "PART", "CENTRAL", "OUT", "WITHIN", "SOCIAL", "THAN", "ACTIVITIES", "ACCESS", "USING",
            "MAY", "VARIOUS", "MANY", "NEEDS", "PUBLISHED", "BOTH", "ONLY"));
    
    public static final ArrayList<String> QUEStopwords = new ArrayList<>(Arrays.asList(
            "OF", "IN", "THE", "AND", "I", "INFORMATION", "INTERESTED", "AM", "ON", "TO", "BE", "WOULD", "FOR",
            "OR", "ANY", "LIBRARY", "A", "LIBRARIES,", "IS", "AT", "RECEIVE", "DISSERTATION", "LIBRARIES",
            "COMPUTER", "WITH", "MY", "LOOKING", "ONLINE", "SYSTEMS,", "THIS", "ESPECIALLY", "RETRIEVAL", "ARE",
            "MANAGEMENT", "PLEASED", "INCLUDE", "SYSTEMS.", "ACADEMIC", "CONSUMER", "BOTH", "ALSO", "WHICH", "SEARCH", "AS",
            "USE", "STUDIES", "DEVELOPED", "PROFESSIONAL", "AREA", "THEIR", "USING",
            "ETC.", "SUCH", "USE,", "I.E.", "RETRIEVAL,", "CHARGING", "WILL", "AN", "BASED", "BY", "MAY", "DATABASES,",
            "LESS", "ASSISTED", "GROUPS,", "BUT", "WHAT", "DOCUMENT", "E.G.", "LIBRARY,", "MORE", "CURRENT", "THAT", "DO", "VARIOUS",
            "INCLUDING", "ALMOST", "CONFLATION", "ALL", "NOT"));

    //Convierte en String todo lo que lea del fichero separado por el regex
    public static void LeerFichero(ArrayList<String> ArrayString, String regex) throws IOException {
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
    
    public static void removeStopwords(ArrayList<String> ArrayString, String regex) throws FileNotFoundException, IOException {
        boolean notNull = false;
        ArrayList<String> Stopwords = new ArrayList<>();
        switch (regex) {
            case regexDocFiles: {
                Stopwords = DOCStopwords;
                notNull = true;
                break;
            }
            case regexQUEfiles: {
                Stopwords = QUEStopwords;
                notNull = true;
                break;
            }
            default: {
                break;
            }
        }
        if (notNull) {
            for (int i = 0; i < ArrayString.size(); i++) {
                for (int j = 0; j < Stopwords.size(); j++) {
                    String regexStop="\\b"+Stopwords.get(j)+"\\b";
                    String reemplazo = ArrayString.get(i).replaceAll(regexStop, "");
                    ArrayString.set(i, reemplazo);
                }
            }
        }
    }

    //FICHEROS-------------------------------------------------------------------------------
    //Crea fichero trec_rel_file
    public static void parseNUM() throws IOException {
        ArrayList<String> NUMFileToString = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();
        
        LeerFichero(NUMFileToString, regexNUMfiles);
        
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

    //Crea QUE para trec_top_file
    public static void QUEsForTrec_top_file() throws SolrServerException, IOException {
        ArrayList<String> QUEFilesToString = new ArrayList<>();
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
        ClienteSolrj solrj = new ClienteSolrj();
        solrj.CreateTrec_top_file(TodasQUE);
    }

    //Crea Fichero con las Stopwords
    public static void StopWordsFile(String regex, int minFrequency, String filename) throws IOException {
        ArrayList<String> DocFileToString = new ArrayList<>();
        ArrayList<String> TodasPalabras = new ArrayList<>();
        
        LeerFichero(DocFileToString, regex);
        //Lee los ficheros de Documentos y 
        for (int i = 0; i < DocFileToString.size(); i++) {
            StringTokenizer st = new StringTokenizer(DocFileToString.get(i));
            while (st.hasMoreTokens()) {
                TodasPalabras.add(st.nextToken());
            }
        }

        //Elimina Duplicados
        Set<String> uniqueSet = new HashSet<>(TodasPalabras);

        //Crea una lista sin duplicados y los ordena por Frecuencia de aparicion
        ArrayList<String> sortedList = new ArrayList<>(uniqueSet);
        Collections.sort(sortedList, (o1, o2) -> {
            return Collections.frequency(TodasPalabras, o2) - Collections.frequency(TodasPalabras, o1);
        });
        
        FileWriter fw = new FileWriter(filename);
        for (String s : sortedList) {
            int frecuencia = Collections.frequency(TodasPalabras, s);
            if (frecuencia >= minFrequency) {
                fw.write(s + " : " + frecuencia + "\n");
                //System.out.println(s + ": " + frecuencia);  
            }
        }
        fw.close();
    }
    
    public static void main(String[] args) throws IOException {
        //StopWordsFile(regexDocFiles,350,"DOC_Stopwords.txt");
        //StopWordsFile(regexQUEfiles,3,"QUE_Stopwords.txt");
    }
}
