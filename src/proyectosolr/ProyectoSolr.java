package proyectosolr;

import TipoDatos.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import org.apache.solr.client.solrj.SolrServerException;

public class ProyectoSolr {
    
    private ClienteSolrj solrj;
    
    //Atributos para Documentos
    private final String regexDocFiles = "glob:**LISA0.0*";// Solo lee 1 fichcero
    private final String regexParseDocs = "(?<=Document\\s{1,4}[0-9]{1,4})\r\n|\r\n\r\n|\n\\*{44}\r\n";
    //private String regexDocFiles = "glob:**LISA[0-5]*";
    private ArrayList<String> DocFilesToString;
    
    //Atributos para Queries
    private ArrayList<String> QUEFilesToString;
    private final String regexDocQUE = "glob:**LISA.QUE*";
    private final String regexParseQUE = "(?<=(?m)^[0-9]{1,2})\r\n|\\s#\r\n";
    
    
    //Convierte en String todo lo que lea del fichero separado por el regex
    private void LeerFichero(ArrayList<String> ArrayString,String regex) throws IOException{
        //Rellena pathFichero con el path de cada fichero dado por la expresion regular "LISA[0-5]* "
        FileSystem fs = FileSystems.getDefault();
        PathMatcher pm = fs.getPathMatcher(regex);

        //Lee todos los ficheros de la carpeta Coleccion Lisa revisada
        //Si el fichero coincide con la expresion regular se pasa el contendio a String para parsearlo
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
        solrj = sol;
        
        LeerFichero(DocFilesToString,regexDocFiles);
    }
    
    public void leerQueries() throws IOException{
        LeerFichero(QUEFilesToString, regexDocQUE);
    }
    
    public void parseQUE() throws SolrServerException, IOException{
        String[] result;
        //Lista donde se van guardando los Documentos
        ArrayList<TipoQuery> TodasQUE = new ArrayList<>();
        
        for (int i = 0; i < QUEFilesToString.size(); i++) {
            result = QUEFilesToString.get(i).split(regexParseQUE);
            
            TipoQuery auxQUE;
            //Recorre 35 queries -->
            //result[0] -- Numero query
            //result[1] -- Texto query
            for (int j = 0; j < result.length - 1; j+=2) {
                auxQUE = new TipoQuery(result[j].replaceAll("\r\n|\r|\n",""),
                                       result[j+1].replaceAll("\r\n|\r|\n"," "));
                
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
                
                auxDoc.addPair("title", result[j+1].replaceAll("\r\n|\r|\n"," "));
                auxDoc.addPair("text", result[j+2].replaceAll("\r\n|\r|\n"," "));

                TodosDocs.add(auxDoc);
            }

        }
        //Indexa 500 Documentos de 1 fichero
        solrj.AddDoc(TodosDocs); //Indexar documento a solr

    }

    
    
    
    public static void main(String[] args) throws IOException, SolrServerException, InterruptedException {
        ProyectoSolr pd = new ProyectoSolr(new ClienteSolrj());

        pd.parseDocs();
        Thread.sleep(300);
        pd.leerQueries();
        pd.parseQUE();
    }

}
