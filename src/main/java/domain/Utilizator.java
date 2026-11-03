package domain;

import java.util.ArrayList;
import java.util.List;

public class Utilizator extends Entity<Long> {
    private final String prenume;
    private final String nume;
    private final List<Utilizator> prieteni = new ArrayList<>();

    public Utilizator(String prenume, String nume) {
        this.prenume = prenume;
        this.nume = nume;
    }

    public String getPrenume() {
        return prenume;
    }

    public String getNume() {
        return nume;
    }

    public List<Utilizator> getPrieteni() {
        return prieteni;
    }

    @Override
    public String toString() {
        return getId() + ". " + prenume + " " + nume;
    }
}
