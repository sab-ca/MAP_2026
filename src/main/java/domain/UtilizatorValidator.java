package domain;

import exceptions.ValidationException;

public class UtilizatorValidator implements Validator<Utilizator> {

    public UtilizatorValidator() {
    }

    @Override
    public void validate(Utilizator u) {
        if (u == null) {
            throw new ValidationException("Utilizatorul nu poate fi null.");
        }
        String erori = "";
        if (u.getId() == null || u.getId() <= 0) {
            erori += "id invalid; ";
        }
        if (!esteNumeValid(u.getPrenume())) {
            erori += "prenume invalid (trebuie sa inceapa cu litera mare); ";
        }
        if (!esteNumeValid(u.getNume())) {
            erori += "nume invalid (trebuie sa inceapa cu litera mare); ";
        }
        if (!erori.isEmpty()) {
            throw new ValidationException(erori.trim());
        }
    }

    private static boolean esteNumeValid(String s) {
        return s != null && !s.isBlank() && Character.isUpperCase(s.charAt(0)) && !s.contains(";");
    }
}
