package domain;

import java.time.LocalDateTime;

public class Prietenie extends Entity<Tuple<Long, Long>> {
    private final LocalDateTime dataPrieteniei;

    public Prietenie(Long idUtilizator1, Long idUtilizator2, LocalDateTime dataPrieteniei) {
        setId(idPentru(idUtilizator1, idUtilizator2));
        this.dataPrieteniei = dataPrieteniei;
    }

    public static Tuple<Long, Long> idPentru(Long idUtilizator1, Long idUtilizator2) {
        if (idUtilizator1 != null && idUtilizator2 != null && idUtilizator1 > idUtilizator2) {
            return new Tuple<>(idUtilizator2, idUtilizator1);
        }
        return new Tuple<>(idUtilizator1, idUtilizator2);
    }

    public Long getIdUtilizator1() {
        return getId().getLeft();
    }

    public Long getIdUtilizator2() {
        return getId().getRight();
    }

    public LocalDateTime getDataPrieteniei() {
        return dataPrieteniei;
    }

    @Override
    public String toString() {
        return getIdUtilizator1() + " - " + getIdUtilizator2() + " (din " + dataPrieteniei + ")";
    }
}
