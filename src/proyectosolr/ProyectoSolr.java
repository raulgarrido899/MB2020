package proyectosolr;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import org.apache.solr.client.solrj.SolrServerException;

public class ProyectoSolr {

    private final String regexFichero = "glob:**LISA0.0*"; //Expresion regular para la primera parte de la practica // Solo lee 1 fichcero
    private final String regexParseDocs = "(?<=Document\\s{1,4}[0-9]{1,4})\r\n|\r\n\r\n|\n\\*{44}\r\n";
    //private String regexFichero = "glob:**LISA[0-5]*";
    private ArrayList<String> FicheroToString;
    private ClienteSolrj solrj;

    //LEE FICHERO ---------------------------------------------------------------------------------------------
    //Al crearse el objeto lee todos los ficheros que recoja la expresion regular de arriba
    public ProyectoSolr(ClienteSolrj sol) throws IOException {
        FicheroToString = new ArrayList<>();
        solrj = sol;

        //Rellena pathFichero con el path de cada fichero dado por la expresion regular "LISA[0-5]* "
        FileSystem fs = FileSystems.getDefault();
        PathMatcher pm = fs.getPathMatcher(regexFichero);

        //Lee todos los ficheros de la carpeta Coleccion Lisa revisada
        //Si el fichero coincide con la expresion regular se pasa el contendio a String para parsearlo
        File directorio = new File("./Coleccion LISA/");
        File[] arrayFicheros = directorio.listFiles();
        for (File FilePath : arrayFicheros) {
            if (pm.matches(FilePath.toPath())) {
                FicheroToString.add(new String(Files.readAllBytes(FilePath.toPath())));
            }
        }
    }

    /*
    //METODO DE PRUEBA para comprobar los ficheros que lee
    public void mostrarFicheroToString() {
        System.out.println("Ficheros leidos: " + FicheroToString.size());
        for (int i = 0; i < FicheroToString.size(); i++) {
            System.out.println(FicheroToString.get(i));
        }
    }
     */
    //FIN LEE FICHERO ---------------------------------------------------------------------------------------------
    //PARSEO FICHEROS ---------------------------------------------------------------------------------------------
    public void parseDocs() throws SolrServerException, IOException {
        String[] result;
        //Lista donde se van guardando los Documentos
        ArrayList<TipoDocumento> TodosDocs = new ArrayList<>();

        //Para cada fichero leido se parsea y se guarda en result
        for (int i = 0; i < FicheroToString.size(); i++) {
            //Parsea 500 Documentos de 1 fichero
            result = FicheroToString.get(i).split(regexParseDocs);

            TipoDocumento auxDoc;
           
            //Recorre 500 Documentos donde --->
            //result[0] -- Numero Documento
            //result[1] -- Titulo Documento
            //result[2] -- Texto Documento
            for (int j = 0; j < result.length - 1; j += 3) {
                auxDoc = new TipoDocumento();
                
                String[] cadenaId = result[j].split("Document\\s{1,4}");

                auxDoc.addPair("id", cadenaId[1]);
                auxDoc.addPair("title", result[j + 1]);
                auxDoc.addPair("text", result[j + 2]);

                TodosDocs.add(auxDoc);
            }

        }
        //Indexa 500 Documentos de 1 fichero
        solrj.AddDoc(TodosDocs); //Indexar documento a solr

    }

    //FIN PARSEO FICHEROS ---------------------------------------------------------------------------------------------
    public static void main(String[] args) throws IOException, SolrServerException {
        ProyectoSolr pd = new ProyectoSolr(new ClienteSolrj());

//        pd.mostrarFicheroToString();
        pd.parseDocs();

    }

}
