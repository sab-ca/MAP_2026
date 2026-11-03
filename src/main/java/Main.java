import domain.Prietenie;
import domain.PrietenieValidator;
import domain.Tuple;
import domain.Utilizator;
import domain.UtilizatorValidator;
import domain.Validator;
import exceptions.RepositoryException;
import exceptions.ServiceException;
import exceptions.ValidationException;
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

            Validator<Utilizator> lungimeNume = u -> {
                if (u.getPrenume().length() > 30 || u.getNume().length() > 30) {
                    throw new ValidationException("prenumele si numele pot avea cel mult 30 de caractere;");
                }
            };
            Validator<Utilizator> validatorUtilizator = new UtilizatorValidator().andThen(lungimeNume);

            Repository<Long, Utilizator> repoUtilizatori = factory.createUtilizatorRepository(validatorUtilizator);
            Repository<Tuple<Long, Long>, Prietenie> repoPrietenii =
                    factory.createPrietenieRepository(new PrietenieValidator());
            ServiceRetea service = new ServiceRetea(repoUtilizatori, repoPrietenii);
            new Consola(service).ruleaza();
        } catch (RepositoryException | ServiceException | UnsupportedOperationException e) {
            System.out.println("Aplicatia nu a putut porni. " + e.getMessage());
        }
    }
}
