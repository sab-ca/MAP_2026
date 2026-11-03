package domain;

import java.util.function.Predicate;

public final class CriteriiUtilizator {

    private CriteriiUtilizator() {
    }

    public static Predicate<Utilizator> faraPrieteni() {
        return u -> u.getPrieteni().isEmpty();
    }

    public static Predicate<Utilizator> areCelPutinPrieteni(int numar) {
        return u -> u.getPrieteni().size() >= numar;
    }

    public static Predicate<Utilizator> numeIncepeCu(String prefix) {
        return u -> u.getNume().toLowerCase().startsWith(prefix.toLowerCase());
    }

    public static Predicate<Utilizator> prenumeIncepeCu(String prefix) {
        return u -> u.getPrenume().toLowerCase().startsWith(prefix.toLowerCase());
    }

    public static Predicate<Utilizator> numeSauPrenumeIncepeCu(String prefix) {
        return numeIncepeCu(prefix).or(prenumeIncepeCu(prefix));
    }
}
