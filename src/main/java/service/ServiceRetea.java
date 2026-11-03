package service;

import domain.Prietenie;
import domain.Tuple;
import domain.Utilizator;
import exceptions.RepositoryException;
import exceptions.ServiceException;
import exceptions.ValidationException;
import repository.Repository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServiceRetea {
    private final Repository<Long, Utilizator> repoUtilizatori;
    private final Repository<Tuple<Long, Long>, Prietenie> repoPrietenii;

    public ServiceRetea(Repository<Long, Utilizator> repoUtilizatori,
                        Repository<Tuple<Long, Long>, Prietenie> repoPrietenii) {
        this.repoUtilizatori = repoUtilizatori;
        this.repoPrietenii = repoPrietenii;
        for (Prietenie p : repoPrietenii.findAll()) {
            Utilizator u1 = repoUtilizatori.findOne(p.getIdUtilizator1());
            Utilizator u2 = repoUtilizatori.findOne(p.getIdUtilizator2());
            if (u1 == null || u2 == null) {
                throw new ServiceException("Date inconsistente: prietenia " + p.getId()
                        + " face referire la un utilizator care nu exista.");
            }
            u1.getPrieteni().add(u2);
            u2.getPrieteni().add(u1);
        }
    }

    public Iterable<Utilizator> getUtilizatori() {
        return repoUtilizatori.findAll();
    }

    public Iterable<Prietenie> getPrietenii() {
        return repoPrietenii.findAll();
    }

    public Utilizator getUtilizator(Long id) {
        Utilizator u = repoUtilizatori.findOne(id);
        if (u == null) {
            throw new ServiceException("Utilizatorul " + id + " nu exista.");
        }
        return u;
    }

    public Utilizator adaugaUtilizator(String prenume, String nume) {
        Utilizator u = new Utilizator(prenume, nume);
        long idMaxim = 0;
        for (Utilizator x : repoUtilizatori.findAll()) {
            idMaxim = Math.max(idMaxim, x.getId());
        }
        u.setId(idMaxim + 1);
        repoUtilizatori.save(u);
        return u;
    }

    public Utilizator stergeUtilizator(Long id) {
        Utilizator u = getUtilizator(id);
        for (Utilizator prieten : new ArrayList<Utilizator>(u.getPrieteni())) {
            stergePrieten(id, prieten.getId());
        }
        repoUtilizatori.delete(id);
        return u;
    }

    public Prietenie adaugaPrieten(Long id1, Long id2) {
        Utilizator u1 = getUtilizator(id1);
        Utilizator u2 = getUtilizator(id2);
        Prietenie p = new Prietenie(id1, id2, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        if (repoPrietenii.save(p) != null) {
            throw new ServiceException("Utilizatorii " + id1 + " si " + id2 + " sunt deja prieteni.");
        }
        u1.getPrieteni().add(u2);
        u2.getPrieteni().add(u1);
        return p;
    }

    public void stergePrieten(Long id1, Long id2) {
        if (repoPrietenii.delete(Prietenie.idPentru(id1, id2)) == null) {
            throw new ServiceException("Utilizatorii " + id1 + " si " + id2 + " nu sunt prieteni.");
        }
        Utilizator u1 = repoUtilizatori.findOne(id1);
        Utilizator u2 = repoUtilizatori.findOne(id2);
        u1.getPrieteni().remove(u2);
        u2.getPrieteni().remove(u1);
    }

    public int numarComunitati() {
        return comunitati().size();
    }

    public List<Utilizator> ceaMaiSociabilaComunitate() {
        List<Utilizator> rezultat = new ArrayList<>();
        int lungimeMaxima = -1;
        for (List<Utilizator> comunitate : comunitati()) {
            int lungime = 0;
            for (Utilizator u : comunitate) {
                lungime = Math.max(lungime, celMaiLungDrum(u, new HashSet<Utilizator>()));
            }
            if (lungime > lungimeMaxima) {
                lungimeMaxima = lungime;
                rezultat = comunitate;
            }
        }
        return rezultat;
    }

    private List<List<Utilizator>> comunitati() {
        List<List<Utilizator>> rezultat = new ArrayList<>();
        Set<Utilizator> vizitati = new HashSet<>();
        for (Utilizator u : repoUtilizatori.findAll()) {
            if (!vizitati.contains(u)) {
                List<Utilizator> comunitate = new ArrayList<>();
                dfs(u, vizitati, comunitate);
                rezultat.add(comunitate);
            }
        }
        return rezultat;
    }

    private void dfs(Utilizator u, Set<Utilizator> vizitati, List<Utilizator> comunitate) {
        vizitati.add(u);
        comunitate.add(u);
        for (Utilizator prieten : u.getPrieteni()) {
            if (!vizitati.contains(prieten)) {
                dfs(prieten, vizitati, comunitate);
            }
        }
    }

    private int celMaiLungDrum(Utilizator u, Set<Utilizator> peDrum) {
        peDrum.add(u);
        int maxim = 0;
        for (Utilizator prieten : u.getPrieteni()) {
            if (!peDrum.contains(prieten)) {
                maxim = Math.max(maxim, 1 + celMaiLungDrum(prieten, peDrum));
            }
        }
        peDrum.remove(u);
        return maxim;
    }
}
