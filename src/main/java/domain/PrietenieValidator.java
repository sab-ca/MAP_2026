package domain;

import exceptions.ValidationException;

public class PrietenieValidator implements Validator<Prietenie> {

    public PrietenieValidator() {
    }

    @Override
    public void validate(Prietenie p) {
        if (p == null) {
            throw new ValidationException("Prietenia nu poate fi null.");
        }
        String erori = "";
        if (p.getIdUtilizator1() == null || p.getIdUtilizator2() == null) {
            erori += "id-urile utilizatorilor nu pot fi null; ";
        } else if (p.getIdUtilizator1().equals(p.getIdUtilizator2())) {
            erori += "un utilizator nu poate fi prieten cu el insusi; ";
        }
        if (p.getDataPrieteniei() == null) {
            erori += "data prieteniei lipseste; ";
        }
        if (!erori.isEmpty()) {
            throw new ValidationException(erori.trim());
        }
    }
}
