package Resources;

import java.util.ArrayList;

public class Persone {
    private ArrayList<Persona> arrayPersone = new ArrayList<>();

    public void addPersona(Persona p) {
        arrayPersone.add(p);
    }
    
    public ArrayList<Persona> getArrayPersone() {
        return arrayPersone;
    }

    public void setArrayPersone(ArrayList<Persona> arrayPersone) {
        this.arrayPersone = arrayPersone;
    }
}
