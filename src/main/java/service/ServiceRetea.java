package service;

import domain.Prietenie;
import domain.Tuple;
import domain.Utilizator;
import exceptions.ServiceException;
import repository.Repository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ServiceRetea {
    private final Repository<Long, Utilizator> repoUtilizatori;
    private final Repository<Tuple<Long, Long>, Prietenie> repoPrietenii;

    public ServiceRetea(Repository<Long, Utilizator> repoUtilizatori,
                        Repository<Tuple<Long, Long>, Prietenie> repoPrietenii) {
        this.repoUtilizatori = repoUtilizatori;
        this.repoPrietenii = repoPrietenii;
        Repository.streamOf(repoPrietenii.findAll()).forEach(this::leagaPrieteni);
    }

    private void leagaPrieteni(Prietenie p) {
        Utilizator u1 = cautaUtilizator(p.getIdUtilizator1()).orElseThrow(() -> inconsistenta(p));
        Utilizator u2 = cautaUtilizator(p.getIdUtilizator2()).orElseThrow(() -> inconsistenta(p));
        u1.getPrieteni().add(u2);
        u2.getPrieteni().add(u1);
    }

    private static ServiceException inconsistenta(Prietenie p) {
        return new ServiceException("Date inconsistente: prietenia " + p.getId()
                + " face referire la un utilizator care nu exista.");
    }

    public List<Utilizator> getUtilizatori() {
        return Repository.streamOf(repoUtilizatori.findAll())
                .sorted(Comparator.comparing(Utilizator::getId))
                .collect(Collectors.toList());
    }

    public List<Prietenie> getPrietenii() {
        return Repository.streamOf(repoPrietenii.findAll())
                .sorted(Comparator.comparing(Prietenie::getDataPrieteniei)
                        .thenComparing(Prietenie::getIdUtilizator1)
                        .thenComparing(Prietenie::getIdUtilizator2))
                .collect(Collectors.toList());
    }

    public List<Utilizator> getPrieteni(Long id) {
        return getUtilizator(id).getPrieteni().stream()
                .sorted(Comparator.comparing(Utilizator::getNume).thenComparing(Utilizator::getPrenume))
                .collect(Collectors.toList());
    }

    public List<Utilizator> cautaUtilizatori(Predicate<Utilizator> criteriu) {
        return repoUtilizatori.findAll(criteriu).stream()
                .sorted(Comparator.comparing(Utilizator::getNume).thenComparing(Utilizator::getPrenume))
                .collect(Collectors.toList());
    }

    public Optional<Utilizator> cautaUtilizator(Long id) {
        return Optional.ofNullable(repoUtilizatori.findOne(id));
    }

    public Utilizator getUtilizator(Long id) {
        return cautaUtilizator(id)
                .orElseThrow(() -> new ServiceException("Utilizatorul " + id + " nu exista."));
    }

    public Utilizator adaugaUtilizator(String prenume, String nume) {
        Utilizator u = new Utilizator(prenume, nume);
        u.setId(Repository.streamOf(repoUtilizatori.findAll())
                .map(Utilizator::getId)
                .max(Comparator.naturalOrder())
                .map(idMaxim -> idMaxim + 1)
                .orElse(1L));
        repoUtilizatori.save(u);
        return u;
    }

    public Utilizator stergeUtilizator(Long id) {
        Utilizator u = getUtilizator(id);
        List<Long> idPrieteni = u.getPrieteni().stream()
                .map(Utilizator::getId)
                .collect(Collectors.toList());
        idPrieteni.forEach(idPrieten -> stergePrieten(id, idPrieten));
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
        Optional.ofNullable(repoPrietenii.delete(Prietenie.idPentru(id1, id2)))
                .orElseThrow(() -> new ServiceException("Utilizatorii " + id1 + " si " + id2 + " nu sunt prieteni."));
        Utilizator u1 = getUtilizator(id1);
        Utilizator u2 = getUtilizator(id2);
        u1.getPrieteni().remove(u2);
        u2.getPrieteni().remove(u1);
    }

    public long numarComunitati() {
        return comunitati().size();
    }

    public List<Utilizator> ceaMaiSociabilaComunitate() {
        return comunitati().stream()
                .max(Comparator.comparingInt(this::celMaiLungDrumDinComunitate))
                .map(comunitate -> comunitate.stream()
                        .sorted(Comparator.comparing(Utilizator::getId))
                        .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    private List<Set<Utilizator>> comunitati() {
        return Repository.streamOf(repoUtilizatori.findAll())
                .map(this::comunitateaLui)
                .distinct()
                .collect(Collectors.toList());
    }

    private Set<Utilizator> comunitateaLui(Utilizator u) {
        Set<Utilizator> vizitati = new HashSet<>();
        dfs(u, vizitati);
        return vizitati;
    }

    private void dfs(Utilizator u, Set<Utilizator> vizitati) {
        if (vizitati.add(u)) {
            u.getPrieteni().forEach(prieten -> dfs(prieten, vizitati));
        }
    }

    private int celMaiLungDrumDinComunitate(Set<Utilizator> comunitate) {
        return comunitate.stream()
                .mapToInt(u -> celMaiLungDrum(u, new HashSet<>()))
                .max()
                .orElse(0);
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
