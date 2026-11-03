ToySocialNetwork - Laboratorul 4 (MAP)
======================================

Laboratorul 4 este o refactorizare: aplicatia face exact ce facea la laboratorul 3
(aceleasi functionalitati, aceleasi fisiere de date, aceleasi mesaje de eroare), dar codul
este rescris in stil Java 8: interfete functionale, lambda, referinte la metode,
Stream API si Optional.

Rulare
------
- IntelliJ: File > Open > folderul ToySocialNetwork (proiect Gradle), apoi Run pe Main.
  In Settings > Build Tools > Gradle: "Build and run using: IntelliJ IDEA".
- Linie de comanda: gradlew build, gradlew run (pe Windows: gradlew.bat ...).
- Date: data/utilizatori.txt (id;prenume;nume), data/prietenii.txt (id1;id2;dataISO).
- Configurare: src/main/resources/config.properties (repository.type=MEMORY|FILE|DATABASE).

Git
---
Proiectul este un repository Git. Istoricul arata pasii refactorizarii:
  git log --oneline
  d783a3e  Lab 3: persistenta in fisier text, RepositoryFactory, config.properties
  eb993ae  Lab 4: Validator functional, findAll(Predicate), criterii, factory cu Map, Files.lines
  8a5f507  Lab 4: stream-uri si Optional in service, consola si Main
  (ultimul commit adauga acest readme)
Comparatie inainte/dupa pentru un fisier:
  git diff d783a3e HEAD -- src/main/java/service/ServiceRetea.java


1. Bucle inlocuite (inainte / dupa)
-----------------------------------
| Clasa.metoda                          | Inainte (Lab 3)                              | Dupa (Lab 4)                                                        |
|---------------------------------------|----------------------------------------------|---------------------------------------------------------------------|
| ServiceRetea (constructor)            | for (Prietenie p : findAll()) {...}          | streamOf(findAll()).forEach(this::leagaPrieteni)                    |
| ServiceRetea.adaugaUtilizator         | for + Math.max pentru id-ul maxim            | map(Utilizator::getId).max(naturalOrder()).map(id -> id + 1).orElse(1L) |
| ServiceRetea.stergeUtilizator         | for peste o copie a listei de prieteni       | stream().map(Utilizator::getId).collect(toList()), apoi forEach     |
| ServiceRetea.comunitati               | for + set vizitati + dfs cu for              | streamOf(findAll()).map(this::comunitateaLui).distinct().collect(toList()) |
| ServiceRetea.ceaMaiSociabilaComunitate| doua for-uri + variabile lungimeMaxima       | stream().max(comparingInt(...)).map(sorted + collect).orElseGet(ArrayList::new) |
| ServiceRetea.dfs                      | for (Utilizator prieten : getPrieteni())     | getPrieteni().forEach(prieten -> dfs(prieten, vizitati))            |
| ServiceRetea.getUtilizatori / getPrietenii | intorceau findAll() (Iterable)          | streamOf(...).sorted(Comparator.comparing(...)...).collect(toList()) |
| AbstractFileRepository.loadData       | while (readLine() != null) + contor linie    | Files.lines(...).collect(toList()) + IntStream.range(...).filter(...).forEach(...) |
| AbstractFileRepository.writeToFile   | for + writer.write + newLine                 | streamOf(findAll()).map(this::createEntityAsString).collect(toList()) + Files.write |
| Consola.afiseazaUtilizatori (opt. 5)  | for + for interior cu String +=              | stream().map(...).forEach(System.out::println), id-uri cu joining(" ") |
| Consola (opt. 6 si 8), meniul         | for / println repetat                        | stream().map(...).forEach(System.out::println), Stream.of(...)       |
| Repository.findAll(Predicate) (nou)   | -                                            | streamOf(findAll()).filter(filter).collect(toList())               |
| RepositoryFactory.create...           | switch (type) { case ...: return new ... }   | Map<RepositoryType, Function<Validator, Repository>> + get(type).apply(validator) |


2. Interfete functionale folosite si unde
-----------------------------------------
Validator<T> (proprie, @FunctionalInterface)
    - metoda default andThen(Validator<T> other) compune doua validari;
    - in Main, regula noua "prenumele si numele au cel mult 30 de caractere" este o lambda
      compusa cu new UtilizatorValidator().andThen(lungimeNume), fara clasa noua si fara
      sa modificam UtilizatorValidator.
Predicate<Utilizator>
    - Repository.findAll(Predicate<E> filter) - metoda default;
    - CriteriiUtilizator: faraPrieteni(), areCelPutinPrieteni(n), numeIncepeCu(p),
      prenumeIncepeCu(p), numeSauPrenumeIncepeCu(p) (construita cu or);
    - Consola.cautaUtilizatori combina criteriile cu and si Predicate.not.
Function<Validator<E>, Repository<ID, E>>
    - RepositoryFactory: cate o functie de creare pentru fiecare RepositoryType, intr-un EnumMap.
Supplier<...>
    - orElseThrow(() -> new ServiceException(...)), orElseGet(ArrayList::new).
Consumer<...>
    - forEach(System.out::println), forEach(this::leagaPrieteni).
Comparator<...>
    - Comparator.comparing(Utilizator::getNume).thenComparing(Utilizator::getPrenume),
      Comparator.comparingInt(this::celMaiLungDrumDinComunitate), Comparator.naturalOrder().


3. Referinte la metode (toate cele patru tipuri)
------------------------------------------------
- metoda statica:                     Long::parseLong, String::valueOf (repository-urile pe fisier)
- metoda de instanta a unui obiect anume: System.out::println (Consola), this::leagaPrieteni,
                                      this::comunitateaLui, this::createEntityAsString
- metoda de instanta a unui obiect oarecare: Utilizator::getId, Utilizator::getNume,
                                      Prietenie::getDataPrieteniei, String::trim
- constructor:                        InMemoryRepository::new (RepositoryFactory), ArrayList::new,
                                      String[]::new, Long[]::new


4. Optional in Repository: am pastrat null
------------------------------------------
Interfata Repository intoarce in continuare null (findOne, save, delete, update), ca la
laboratorul 3 si ca in contractul de la curs. Motive:
- contractul save/update foloseste null cu sens de "succes", iar Optional ar inversa
  sensul (Optional.empty() = succes), ceea ce e greu de citit;
- schelele DB si toate implementarile raman neschimbate, iar Javadoc-ul ramane consecvent;
- Optional apare acolo unde aduce ceva: in service, cautaUtilizator(id) intoarce
  Optional.ofNullable(repo.findOne(id)), iar getUtilizator foloseste orElseThrow;
  stergePrieten foloseste Optional.ofNullable(repo.delete(...)).orElseThrow(...);
  rezultatele lui max sunt folosite cu map / orElse / orElseGet; nu se apeleaza get().


5. Template Method vs Function in repository-urile pe fisier: am pastrat Template Method
-----------------------------------------------------------------------------------------
AbstractFileRepository ramane clasa abstracta cu extractEntity / createEntityAsString
implementate in UtilizatorFileRepository si PrietenieFileRepository. Motive:
- conversia linie <-> entitate are si validare de format (numarul de campuri), care sta
  mai bine intr-o clasa cu nume decat intr-o lambda lunga trimisa la constructor;
- RepositoryFactory si restul aplicatiei nu se schimba;
- alternativa (un singur FileRepository<ID, E> cu Function<String, E> si
  Function<E, String> in constructor) ar fi compozitie in loc de mostenire si ar evita
  apelul de metode suprascrise din constructor; ar fi alegerea mai buna daca am avea
  multe entitati noi de salvat in fisier.


6. Bucle pastrate si de ce
--------------------------
- ServiceRetea.celMaiLungDrum: backtracking recursiv cu for-each. Pasul de backtracking
  (adaug pe drum, recursie, scot de pe drum) modifica setul peDrum; scris cu stream-uri,
  functia din mapToInt ar avea efecte secundare, ceea ce enuntul interzice, si codul s-ar
  citi mai greu. Exceptie permisa explicit in enunt.
- Consola.ruleaza: while (continua) este bucla meniului (repeta pana la optiunea 0), nu o
  parcurgere de colectie.


7. Pipeline-uri: operatii intermediare si terminale (exemple pentru predare)
----------------------------------------------------------------------------
Operatiile intermediare (filter, map, sorted, distinct) intorc un stream nou si sunt
lenese: nu fac nimic pana nu apare o operatie terminala. Operatia terminala (collect,
count, max, forEach) porneste prelucrarea si inchide stream-ul.

a) ServiceRetea.comunitati
   Repository.streamOf(repoUtilizatori.findAll())   sursa
       .map(this::comunitateaLui)                   intermediara: utilizator -> comunitatea lui
       .distinct()                                  intermediara: aceeasi comunitate o singura data
       .collect(Collectors.toList())                terminala: lista de comunitati
b) ServiceRetea.adaugaUtilizator
   Repository.streamOf(repoUtilizatori.findAll())
       .map(Utilizator::getId)                      intermediara
       .max(Comparator.naturalOrder())              terminala: Optional<Long>
       .map(idMaxim -> idMaxim + 1).orElse(1L)      metode ale lui Optional, nu ale stream-ului
c) AbstractFileRepository.writeToFile
   Repository.streamOf(findAll())
       .map(this::createEntityAsString)             intermediara: entitate -> linie
       .collect(Collectors.toList())                terminala: lista de linii pentru Files.write
d) ServiceRetea.cautaUtilizatori
   repoUtilizatori.findAll(criteriu)                filter (intermediara) + collect (terminala)
       .stream().sorted(Comparator.comparing(Utilizator::getNume)
                 .thenComparing(Utilizator::getPrenume))   intermediara
       .collect(Collectors.toList())                terminala


8. Factory fara switch
----------------------
RepositoryFactory tine doua EnumMap-uri: RepositoryType -> Function care primeste
validatorul si creeaza repository-ul. Functiile sunt lambda-uri, deci getRequired("...")
se apeleaza doar pentru tipul ales (la MEMORY nu se cer cai de fisier). Un tip nou de
persistenta inseamna o valoare noua in RepositoryType si o intrare noua in fiecare Map,
fara metode noi si fara switch modificat.


9. Fisier lipsa, linii corupte, exceptii (neschimbat fata de Lab 3)
-------------------------------------------------------------------
- Fisier lipsa: repository gol; fisierul se creeaza la prima salvare.
- Linie corupta: liniile se citesc cu Files.lines intr-un try-with-resources (stream-ul tine
  fisierul deschis), apoi IntStream.range(0, linii.size()) da numarul liniei. Incarcarea se
  opreste cu RepositoryException: fisier, linia, continutul si motivul. Motivul opririi:
  fisierul se rescrie complet la fiecare modificare, deci o linie ignorata s-ar pierde.
- IOException si UncheckedIOException (aruncata de Files.lines in timpul citirii) se
  impacheteaza in RepositoryException, cu exceptia originala drept cauza.
- Exceptiile se prind doar in Consola si in Main.
- Fisierele de test raman in data/test/ (utilizatori_corupt.txt, prietenii_corupt.txt,
  prietenii_inconsistent.txt).


10. Demonstratie la predare
---------------------------
1. Aceleasi rezultate ca la Lab 3: optiunile 7 (4 comunitati) si 8 (Ana, Mihai, Ioana,
   Andrei, Maria), plus adaugari/stergeri si repornire.
2. Inainte/dupa: git diff d783a3e HEAD -- src/main/java/service/ServiceRetea.java
   (si AbstractFileRepository.java, RepositoryFactory.java); explicatie cu sectiunea 7.
3. Regula de validare noua doar prin lambda, in Main:
     Validator<Utilizator> numeDiferitDePrenume = u -> {
         if (u.getNume().equals(u.getPrenume())) {
             throw new ValidationException("numele nu poate fi egal cu prenumele;");
         }
     };
     ... new UtilizatorValidator().andThen(lungimeNume).andThen(numeDiferitDePrenume)
4. Criteriu de cautare nou doar prin lambda: optiunea 9 din meniu foloseste criterii
   combinate; un criteriu nou se scrie direct, de exemplu
     service.cautaUtilizatori(CriteriiUtilizator.areCelPutinPrieteni(2).and(u -> u.getId() > 3))
5. repository.type=MEMORY si DATABASE se comporta ca la Lab 3
   (DATABASE: "Persistenta in baza de date nu este implementata inca (laboratorul 5).").


11. Principii si sabloane (actualizat)
--------------------------------------
- Strategy: Validator (acum si ca lambda, compus cu andThen).
- Template Method: AbstractFileRepository + subclasele pe fisier.
- Factory: RepositoryFactory, cu Map de functii in loc de switch.
- DIP: service-ul depinde de Repository, repository-urile de Validator.
- OCP: o regula de validare sau un criteriu de cautare nou este o lambda noua, fara
  modificarea claselor existente.
