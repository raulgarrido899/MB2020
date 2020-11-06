
package TipoDatos;

import java.util.ArrayList;
import java.util.Arrays;

public class TipoDocumento {
    //Conjunto de Nombres de variables y sus valores
    private ArrayList<String> FieldName;
    private ArrayList<String> FieldValue;
    
    public TipoDocumento(){
        FieldName = new ArrayList<>();
        FieldValue = new ArrayList<>();
    }
    
    public int getNumFields(){
        return FieldName.size();
    }
    
    /*return Arraylist[0] -> Name,Arraylist[1] -> Value
    */
    public ArrayList<String> getPair(int index){
       return new ArrayList<>(Arrays.asList(FieldName.get(index),FieldValue.get(index)));
    }
    
    public void addPair(String name,String value){
        FieldName.add(name);
        FieldValue.add(value);
    }
   


    
}
