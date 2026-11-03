package ui;

import domain.CriteriiUtilizator;
import domain.Prietenie;
import domain.Utilizator;
import exceptions.RepositoryException;
import exceptions.ServiceException;
import exceptions.ValidationException;
import service.ServiceRetea;

import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Consola {
    private final ServiceRetea service;
    private final Scanner scanner = new Scanner(System.in);

    public Consola(ServiceRetea service) {
        this.service = service;
    }

    public void ruleaza() {
        boolean continua = true;
        while (continua) {
            afiseazaMeniu();
            String optiune = citeste("Optiune: ");
            try {
                continua = executa(optiune);
            } catch (ValidationException e) {
                System.out.println("Date invalide: " + e.getMessage());
            } catch (ServiceException e) {
                System.out.println("Eroare: " + e.getMessage());
            } catch (RepositoryException e) {
                System.out.println("Eroare la salvare: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Eroare: id-ul trebuie sa fie un numar.");
            }
        }
    }

    private void afiseazaMeniu() {
        Stream.of("",
                        "1. Adauga utilizator",
                        "2. Sterge utilizator",
                        "3. Adauga prieten",
                        "4. Sterge prieten",
                        "5. Afiseaza utilizatorii",
                        "6. Afiseaza prieteniile",
                        "7. Numar de comunitati",
                        "8. Cea mai sociabila comunitate",
                        "9. Cauta utilizatori",
                        "0. Iesire")
                .forEach(System.out::println);
    }

    private boolean executa(String optiune) {
        switch (optiune) {
            case "1":
                String prenume = citeste("Prenume: ");
                String nume = citeste("Nume: ");
                System.out.println("Adaugat: " + service.adaugaUtilizator(prenume, nume));
                return true;
            case "2":
                System.out.println("Sters: " + service.stergeUtilizator(citesteId("Id utilizator: ")));
                return true;
            case "3":
                Prietenie p = service.adaugaPrieten(citesteId("Id primul utilizator: "),
                        citesteId("Id al doilea utilizator: "));
                System.out.println("Prietenie adaugata: " + p);
                return true;
            case "4":
                service.stergePrieten(citesteId("Id primul utilizator: "), citesteId("Id al doilea utilizator: "));
                System.out.println("Prietenie stearsa.");
                return true;
            case "5":
                service.getUtilizatori().stream()
                        .map(u -> "  " + u + " - prieteni: " + idPrieteni(u))
                        .forEach(System.out::println);
                return true;
            case "6":
                service.getPrietenii().stream()
                        .map(x -> "  " + x)
                        .forEach(System.out::println);
                return true;
            case "7":
                System.out.println("Numar de comunitati: " + service.numarComunitati());
                return true;
            case "8":
                System.out.println("Cea mai sociabila comunitate:");
                afiseaza(service.ceaMaiSociabilaComunitate());
                return true;
            case "9":
                cautaUtilizatori();
                return true;
            case "0":
                return false;
            default:
                System.out.println("Optiune invalida.");
                return true;
        }
    }

    private void cautaUtilizatori() {
        String prefix = citeste("Numele sau prenumele incepe cu (Enter = oricare): ");
        String prieteni = citeste("Prieteni: d = doar fara prieteni, c = doar cu prieteni, Enter = oricare: ");
        Predicate<Utilizator> criteriu = u -> true;
        if (!prefix.isEmpty()) {
            criteriu = criteriu.and(CriteriiUtilizator.numeSauPrenumeIncepeCu(prefix));
        }
        if (prieteni.equalsIgnoreCase("d")) {
            criteriu = criteriu.and(CriteriiUtilizator.faraPrieteni());
        } else if (prieteni.equalsIgnoreCase("c")) {
            criteriu = criteriu.and(Predicate.not(CriteriiUtilizator.faraPrieteni()));
        }
        List<Utilizator> gasiti = service.cautaUtilizatori(criteriu);
        System.out.println("Gasiti: " + gasiti.size());
        afiseaza(gasiti);
    }

    private void afiseaza(List<Utilizator> utilizatori) {
        utilizatori.stream()
                .map(u -> "  " + u)
                .forEach(System.out::println);
    }

    private String idPrieteni(Utilizator u) {
        return u.getPrieteni().stream()
                .map(Utilizator::getId)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));
    }

    private String citeste(String mesaj) {
        System.out.print(mesaj);
        if (!scanner.hasNextLine()) {
            return "0";
        }
        return scanner.nextLine().trim();
    }

    private Long citesteId(String mesaj) {
        return Long.parseLong(citeste(mesaj));
    }
}
