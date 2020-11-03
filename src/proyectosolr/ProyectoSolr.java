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
    private final String regexParseDocs = "(?<=[0-9]\r)|\n\\*{44}";
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
        // el (?<=) no elimina el delimitador del split -> stackOverflow
        System.out.println("Numero de ficheros: " + FicheroToString.size());

        //Para cada fichero leido se parsea
        for (int i = 0; i < FicheroToString.size(); i++) {
            result = FicheroToString.get(i).split(regexParseDocs);
            System.out.println("Numero de splits: " + result.length);

            ArrayList<String> fieldName = new ArrayList<>();
            ArrayList<String> value = new ArrayList<>();
            //Recorre 
            for (int j = 0; j < result.length; j++) {
                //result[0] -- Numero Documento
                if (j % 2 == 0) {
                    fieldName.add("title");
                    value.add(result[j]);
//                    System.out.println("Documento------------------------------------>>>>>>>>");
//                    System.out.println(result[j]);

                } //result[1] -- Contenido Documento
                else if (j % 2 == 1) {
                    fieldName.add("doc");
                    value.add(result[j]);
//                    System.out.println("Texto--------------------------------------::::::::::");
//                    System.out.println(result[j]);

                }
            }
            solrj.AddDoc(fieldName, value); //Indexar documento a solr
        }

    }

    //FIN PARSEO FICHEROS ---------------------------------------------------------------------------------------------
    public static void main(String[] args) throws IOException, SolrServerException {
        ProyectoSolr pd = new ProyectoSolr(new ClienteSolrj());

//        pd.mostrarFicheroToString();
        pd.parseDocs();

    }

}
