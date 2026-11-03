package ui;

import domain.Prietenie;
import domain.Utilizator;
import exceptions.RepositoryException;
import exceptions.ServiceException;
import exceptions.ValidationException;
import service.ServiceRetea;

import java.util.Scanner;

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
        System.out.println();
        System.out.println("1. Adauga utilizator");
        System.out.println("2. Sterge utilizator");
        System.out.println("3. Adauga prieten");
        System.out.println("4. Sterge prieten");
        System.out.println("5. Afiseaza utilizatorii");
        System.out.println("6. Afiseaza prieteniile");
        System.out.println("7. Numar de comunitati");
        System.out.println("8. Cea mai sociabila comunitate");
        System.out.println("0. Iesire");
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
                afiseazaUtilizatori();
                return true;
            case "6":
                for (Prietenie x : service.getPrietenii()) {
                    System.out.println("  " + x);
                }
                return true;
            case "7":
                System.out.println("Numar de comunitati: " + service.numarComunitati());
                return true;
            case "8":
                System.out.println("Cea mai sociabila comunitate:");
                for (Utilizator u : service.ceaMaiSociabilaComunitate()) {
                    System.out.println("  " + u);
                }
                return true;
            case "0":
                return false;
            default:
                System.out.println("Optiune invalida.");
                return true;
        }
    }

    private void afiseazaUtilizatori() {
        for (Utilizator u : service.getUtilizatori()) {
            String prieteni = "";
            for (Utilizator x : u.getPrieteni()) {
                prieteni += x.getId() + " ";
            }
            System.out.println("  " + u + " - prieteni: " + prieteni);
        }
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
