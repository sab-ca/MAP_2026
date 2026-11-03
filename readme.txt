ToySocialNetwork - Laboratorul 3 (MAP)
======================================

Rulare
------
- IntelliJ: File > Open > folderul ToySocialNetwork (se importa ca proiect Gradle),
  apoi Run pe Main (src/main/java/Main.java).
- Linie de comanda: gradlew run   (pe Windows: gradlew.bat run)
- Datele sunt in data/utilizatori.txt (id;prenume;nume) si
  data/prietenii.txt (idUtilizator1;idUtilizator2;dataPrieteniei, data ISO, ex. 2026-10-19T10:30).
- Configurarea este in src/main/resources/config.properties.

Structura
---------
domain       Entity, Tuple, Utilizator, Prietenie, Validator, UtilizatorValidator, PrietenieValidator
exceptions   ValidationException, RepositoryException, ServiceException (toate unchecked)
repository   Repository, InMemoryRepository, AbstractFileRepository,
             UtilizatorFileRepository, PrietenieFileRepository,
             UtilizatorDbRepository, PrietenieDbRepository (schelete pentru laboratorul 4)
factory      RepositoryFactory, RepositoryType
service      ServiceRetea
ui           Consola
Main         construieste aplicatia si porneste consola


1. Cum functioneaza factory-ul
------------------------------
RepositoryFactory este un Simple Factory si este singura clasa care cunoaste clasele
concrete de repository.

- In constructor citeste config.properties de pe classpath cu java.util.Properties,
  intr-un try-with-resources, si transforma repository.type in enum-ul RepositoryType
  (MEMORY, FILE, DATABASE).
- createUtilizatorRepository(validator) si createPrietenieRepository(validator) au un switch
  pe tip si intorc tipul abstract Repository<ID, E>. Ambele folosesc acelasi tip de
  persistenta, citit o singura data.
- O valoare lipsa (repository.type, file.utilizatori, db.url etc.) sau un tip necunoscut
  produce o RepositoryException cu mesaj clar, de exemplu:
  "repository.type are valoarea necunoscuta 'CLOUD'; valori permise: MEMORY, FILE, DATABASE."
- Main cere repository-urile de la factory. Service-ul, UI-ul si Main lucreaza doar cu
  interfata Repository si nu contin niciun new pe un repository concret.

Ce trebuie schimbat pentru trecerea la baza de date (laboratorul 4):
1. Se scrie codul JDBC in metodele din UtilizatorDbRepository si PrietenieDbRepository
   (acum arunca UnsupportedOperationException). Ele primesc deja prin constructor
   validatorul si db.url, db.user, db.password.
2. Se adauga driverul in build.gradle.kts.
3. In config.properties se pune repository.type=DATABASE si datele reale de conectare.
Service-ul, UI-ul si Main raman neschimbate.


2. Fisier lipsa si linii corupte
--------------------------------
Fisier lipsa: AbstractFileRepository verifica Files.exists inainte de citire. Daca fisierul
nu exista, repository-ul porneste gol, iar fisierul (si folderul data/, daca lipseste)
se creeaza la prima salvare.

Linie corupta: incarcarea se opreste cu o RepositoryException care spune fisierul,
numarul liniei, linia si motivul. Main o prinde si afiseaza mesajul, fara stack trace:
  Aplicatia nu a putut porni. data/utilizatori.txt, linia 3 ("abc;Ion;Pop"): format invalid - For input string: "abc"
Sunt tratate: numar gresit de campuri, id care nu e numar (NumberFormatException),
data in format gresit (DateTimeParseException), date care nu trec de validator
(ValidationException) si id duplicat. Exceptia originala este pastrata ca cause
(exception chaining).

De ce oprim incarcarea in loc sa ignoram linia:
fisierul este rescris complet dupa fiecare save, delete sau update. Daca am ignora linia
corupta, ea ar disparea definitiv din fisier la prima modificare, fara ca utilizatorul sa
afle. Oprirea cu un mesaj clar nu pierde date: utilizatorul repara linia (sau o sterge,
constient) si reporneste aplicatia. In plus, exceptia ajunge in UI, care e locul unde se
afiseaza mesajele, iar repository-ul nu scrie nimic pe ecran.

Consistenta intre fisiere:
- o prietenie se adauga doar intre utilizatori existenti (verificat in ServiceRetea);
- stergerea unui utilizator ii sterge intai toate prieteniile, apoi utilizatorul;
- la pornire, daca prietenii.txt contine un utilizator care nu exista in utilizatori.txt,
  aplicatia se opreste cu mesajul "Date inconsistente: prietenia (1, 99) face referire
  la un utilizator care nu exista."

Fisiere de test (in data/test/), folosite prin schimbarea caii in config.properties:
- utilizatori_corupt.txt   linia 3 are id nenumeric, 4 are nume invalide, 5 are doar 2 campuri
- prietenii_corupt.txt     linia 2 are o data invalida
- prietenii_inconsistent.txt  linia 2 face referire la utilizatorul 99, care nu exista
Exemplu: file.utilizatori=data/test/utilizatori_corupt.txt


3. Unde se gasesc principiile SOLID
-----------------------------------
SRP (o singura responsabilitate): fiecare clasa are un singur motiv de schimbare.
    Validatorii valideaza, repository-urile pastreaza datele, AbstractFileRepository
    citeste/scrie fisierul, subclasele lui stiu doar formatul unei linii, factory-ul alege
    implementarea, ServiceRetea are regulile retelei, Consola citeste si afiseaza.
OCP (deschis la extindere, inchis la modificare): o regula noua de validare este o clasa
    noua care implementeaza Validator, fara schimbari in repository. O entitate noua in
    fisier este o subclasa noua a AbstractFileRepository, fara schimbari in codul de
    citire/scriere. Trecerea la baza de date nu schimba service-ul si UI-ul.
LSP (substituirea Liskov): InMemoryRepository, AbstractFileRepository si repository-urile
    DB pot inlocui oricand interfata Repository; implementarile respecta contractul din
    Javadoc (null intors la save reusit, IllegalArgumentException pentru id/entitate null).
    Schimbarea repository.type intre MEMORY si FILE nu schimba comportamentul aplicatiei.
DIP (dependenta de abstractii): ServiceRetea depinde de interfata Repository, iar
    repository-urile depind de interfata Validator. Implementarile concrete sunt alese de
    factory si primite prin constructor.


4. Unde se gasesc sabloanele de proiectare
------------------------------------------
Factory (Simple Factory): RepositoryFactory creeaza repository-urile dupa repository.type.
Template Method: AbstractFileRepository are pasii de incarcare si salvare (loadData,
    writeToFile, save/delete/update); UtilizatorFileRepository si PrietenieFileRepository
    completeaza doar extractEntity si createEntityAsString.
Strategy: Validator este strategia de validare primita de repository in constructor
    (UtilizatorValidator, PrietenieValidator).
Decorator: BufferedReader peste cititorul de fisier (Files.newBufferedReader) adauga buffer
    si readLine(); BufferedWriter peste scriitorul de fisier adauga buffer si newLine().


5. Simple Factory vs Abstract Factory (OCP)
-------------------------------------------
Simple Factory (varianta folosita): o singura clasa cu switch pe tip.
  + simplu de inteles: tot codul de creare e intr-un loc;
  + un singur fisier de citit la predare / depanare;
  - incalca OCP: un tip nou de persistenta (de exemplu JSON) inseamna modificarea
    switch-ului din ambele metode ale RepositoryFactory.
Abstract Factory: interfata RepositoryFactory cu metodele createUtilizatorRepository si
createPrietenieRepository, implementata de MemoryRepositoryFactory, FileRepositoryFactory,
DbRepositoryFactory.
  + respecta OCP: un tip nou de persistenta este o clasa noua, fara modificarea celor existente;
  + garanteaza ca repository-urile create impreuna sunt din aceeasi "familie";
  - mai multe clase pentru putina logica;
  - tot trebuie ales undeva ce fabrica se foloseste (tot un switch, mutat intr-un singur loc).
Pentru 3 tipuri de persistenta, Simple Factory este suficient; daca tipurile s-ar inmulti,
Abstract Factory ar fi alegerea mai buna.


6. Unde se face validarea
-------------------------
Validarea datelor unei entitati (campuri, formate) se face in repository, prin
Validator-ul primit in constructor. Asa trec prin validator si datele incarcate din
fisier, nu doar cele introduse din consola. Regulile care depind de mai multe entitati
(utilizatorii unei prietenii trebuie sa existe, prietenia nu trebuie sa existe deja)
sunt in service, pentru ca un repository nu stie de celalalt.


7. Demonstratie la predare
--------------------------
1. Adaugari si stergeri: optiunile 1-4 din meniu; optiunile 5 si 6 arata datele.
2. Datele se pastreaza: optiunea 0, apoi se reporneste aplicatia si se afiseaza din nou.
3. Fisier lipsa: se redenumeste data/utilizatori.txt -> aplicatia porneste goala;
   dupa o adaugare fisierul apare din nou.
4. Linie corupta: se scrie manual o linie gresita (ex. "abc;Ion;Pop") sau se foloseste
   data/test/utilizatori_corupt.txt -> mesaj cu numarul liniei si motivul.
5. repository.type=MEMORY -> aplicatia merge, dar datele nu se pastreaza dupa repornire.
6. repository.type=DATABASE -> "Persistenta in baza de date nu este implementata inca
   (laboratorul 4)."
