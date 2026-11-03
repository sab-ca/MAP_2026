import domain.Prietenie;
import domain.PrietenieValidator;
import domain.Tuple;
import domain.Utilizator;
import domain.UtilizatorValidator;
import exceptions.RepositoryException;
import exceptions.ServiceException;
import factory.RepositoryFactory;
import repository.Repository;
import service.ServiceRetea;
import ui.Consola;

public class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try {
            RepositoryFactory factory = new RepositoryFactory("config.properties");
            System.out.println("Persistenta: " + factory.getType());
            Repository<Long, Utilizator> repoUtilizatori =
                    factory.createUtilizatorRepository(new UtilizatorValidator());
            Repository<Tuple<Long, Long>, Prietenie> repoPrietenii =
                    factory.createPrietenieRepository(new PrietenieValidator());
            ServiceRetea service = new ServiceRetea(repoUtilizatori, repoPrietenii);
            new Consola(service).ruleaza();
        } catch (RepositoryException e) {
            System.out.println("Aplicatia nu a putut porni. " + e.getMessage());
        } catch (ServiceException e) {
            System.out.println("Aplicatia nu a putut porni. " + e.getMessage());
        } catch (UnsupportedOperationException e) {
            System.out.println("Aplicatia nu a putut porni. " + e.getMessage());
        }
    }
}
