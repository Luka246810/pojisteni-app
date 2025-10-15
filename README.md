# PojištěníApp (Spring Boot + JPA + Thymeleaf)

Jednoduchá evidence pojištěnců, jejich pojistek a pojistných událostí.  
Běží na: **http://localhost:8080**

---

## Požadavky

- **Java (JDK) 21**  
  Ověření: `java -version` → např. `21.x`
- **Maven 3.9+**  
  Ověření: `mvn -v`
- **Lombok** (plugin do IDE – IntelliJ/Eclipse). Bez pluginu může IDE hlásit chybějící gettery/settery.
- **Databáze** – vyber jednu z cest:
    - **Docker** (doporučeno pro nejjednodušší start), **nebo**
    - **lokální MariaDB/MySQL** (XAMPP apod.)

---

## Použité technologie (stručně)

- **Spring Boot 3.3.x**, **Spring Data JPA (Hibernate)**, **Spring Security 6**
- **Thymeleaf** + **Thymeleaf Extras Spring Security** (tagy `sec:*`)
- **Bootstrap 5** + vlastní CSS
- **MariaDB** (lokálně / v Dockeru)
- **Metoda‑level autorizace** přes `@PreAuthorize` (role **ADMIN/USER**)
- **Jakarta Bean Validation** (u registrace)
- **Java records/DTO** pro přenos dat do šablon a reportů

---

## Kde co je v projektu

```
src/main/java/...                  # Java zdrojáky (controller, service, repository, entity, security, dto…)
src/main/resources/templates/...   # Thymeleaf šablony (HTML)
src/main/resources/static/...      # CSS/JS/obrázky
src/main/resources/application.properties       # výchozí konfigurace
src/main/resources/application-demo.properties  # DEMO profil (auto-import DB)
src/main/resources/db/init.sql                  # dump databáze (schéma + demo data)
```

> **init.sql** je SQL dump – aplikace ho umí automaticky použít (viz níže).  
> **Pozn.:** Docker varianta používá **resources/db/init.sql**. Pro ruční import přes phpMyAdmin je v kořeni projektu ještě `projekt_pojistovna.sql`.

---

## Jak spustit (varianta A – **Docker Compose**)

Uživatel nepotřebuje instalovat MariaDB/XAMPP. Vše naběhne **jedním příkazem**.

1. Nainstaluj **Docker Desktop** (Win/Mac) nebo Docker (Linux).
2. V kořeni projektu spusť:
   ```bash
   docker compose up --build
   ```
3. Otevři **http://localhost:8080**

Co se stane:
- Spustí se **MariaDB** v kontejneru a **jednou** se naimportuje `src/main/resources/db/init.sql`.
- Spustí se samotná aplikace (Spring Boot).
- Hned po startu vidíš data obsažená v `init.sql`.

> **Přihlašovací údaje v Docker DB:** uživatel `root`, heslo `root`.  
> Import `init.sql` proběhne jen při **prvním** startu prázdného DB volume.  
> Chceš import zopakovat? Smaž volume nebo přejmenuj SQL na další pořadí (např. `02-init.sql`).

---

## Jak spustit (varianta B – **lokální MariaDB/XAMPP**, DEMO profil s auto-importem)

Nemáš Docker? Nevadí. Aplikace si při startu **sama** naimportuje `init.sql` do lokální DB.

1. Spusť **MariaDB/MySQL** (např. XAMPP).  
   Výchozí očekávané připojení v DEMO profilu:
   ```
   jdbc:mariadb://localhost:3306/projekt_pojistovna_demo
   uživatel: root
   heslo: (prázdné)
   ```
   (Vše lze změnit v `src/main/resources/application-demo.properties`.)
2. V kořeni projektu spusť:
   ```bash
   mvn clean spring-boot:run -Dspring-boot.run.profiles=demo
   ```
3. Otevři **http://localhost:8080**

Co se stane:
- Aplikace **nemění schéma** (má `ddl-auto=none`).
- Spring **automaticky spustí** `classpath:db/init.sql` (tj. `src/main/resources/db/init.sql`) a naplní DB.
- Při opakovaném startu se konfliktní SQL kroky přeskočí (`spring.sql.init.continue-on-error=true`).

> **Pozn.:** V DEMO profilu je výchozí heslo do DB prázdné (uživatel `root`).

---

## Jak spustit (varianta C – **bez dumpu**, čistě z entit)

Chceš prázdnou DB a seedovat si data sám? Nech aplikaci vytvořit tabulky z JPA entit.

1. V `src/main/resources/application.properties` nastav:
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```
2. Spusť:
   ```bash
   mvn clean spring-boot:run
   ```
3. Aplikace vytvoří **prázdné** tabulky; data si založíš ručně (registrace, …).

---

## Přihlášení / registrace

Defaultní (Zkušební) účty: (ROLE ADMIN: admin / admin), (ROLE USER: user / user)  
*(pokud jsou v `init.sql`, jinak si založ uživatele sám přes registraci)*

- Přihlášení: **/login**
- Registrace: **/register**
- Po přihlášení: **/ucet/profil** (nastavení profilu)

> **Role a práva:**  
> Role jsou v tabulce `uzivatel_role (uzivatel_id, role_name)`.  
> Běžný uživatel má `ROLE_USER`. Admin `ROLE_ADMIN`.  
> Vybrané akce jsou chráněné přes anotace `@PreAuthorize`.  
> Pokud jsou v `init.sql` i demo účty, zkontroluj konkrétní username/hesla přímo v souboru (hesla jsou v BCrypt hash).

---

## Reporty a export

Na stránce **/reporty** jsou dostupné agregace (noví pojištěnci po měsících, aktivní pojistky podle typu, škody podle stavu, …).  
Tlačítka **Export CSV** vrací příslušné datasety přímo jako CSV (endpointy v `ReportExportController`).  
Podklady pro reporty využívají **JPQL i native SQL** v `ReportRepo` a přenáší se přes **Java records/DTO**.

---

## Obnova hesla (demo)

Aplikace obsahuje ukázkový reset hesla přes token:
- Zapomenuté heslo: **/auth/forgot-password**
- Reset s tokenem: **/auth/reset-password**

> **Pozn.:** Tokeny jsou drženy pouze in-memory (DEMO). V produkci by se tokeny ukládaly do DB a posílaly e‑mailem.

---

## Nastavení databáze (shrnutí)

- **Docker varianta** – nic nenastavuješ; Compose vytvoří DB a importuje `init.sql`.
- **DEMO profil (lokální MariaDB)** – `src/main/resources/application-demo.properties` (výňatek):
  ```properties
  spring.datasource.url=jdbc:mariadb://localhost:3306/projekt_pojistovna_demo?useUnicode=true&characterEncoding=utf8
  spring.datasource.username=root
  spring.datasource.password=
  spring.jpa.hibernate.ddl-auto=none
  spring.sql.init.mode=always
  spring.sql.init.schema-locations=classpath:db/init.sql
  spring.sql.init.continue-on-error=true
  ```
- **Výchozí profil (bez auto-importu)** – `src/main/resources/application.properties`:
  ```properties
  spring.jpa.hibernate.ddl-auto=none  # používáš předpřipravenou DB (dump ručně naimportovaný nebo Docker)
  # případně 'update' pro automatické vytvoření prázdného schématu
  ```

---

## Build do JARu (volitelné)

Vytvoření spustitelného JARu:
```bash
mvn clean package -DskipTests
java -jar target/pojisteni-app-0.0.1-SNAPSHOT.jar
```

Výchozí port: **8080** (změníš `server.port=...`).

---

## Nejčastější problémy

- **`Communications link failure` / `Connection refused`**  
  → MariaDB neběží nebo je jiný host/port. Zkontroluj `spring.datasource.url`.
- **`Access denied for user`**  
  → Špatné přihlašovací údaje (username/heslo).
- **`Table '...uzivatel' doesn't exist`**  
  → Nespouštíš Docker ani DEMO profil (auto-import) a zároveň jsi nenaimportoval dump ručně.  
  Řešení: použij Docker **nebo** DEMO profil **nebo** nastav `ddl-auto=update`.
- **Docker se nenaimportoval**  
  → `init.sql` se spouští jen při *prvním* startu prázdného volume. Smaž volume (`docker volume rm …`) a pusť znovu, nebo změň název SQL na další pořadí (např. `02-init.sql`).

---

## Licence

Projekt je pod licencí **MIT** (viz soubor `LICENSE` v kořeni).

---

### Rychlý start

A) **Ruční import (phpMyAdmin):**  
Otevři phpMyAdmin → Import → vyber **`projekt_pojistovna.sql`** (soubor v kořeni projektu).

B) **Docker (doporučeno):**
```bash
docker compose up --build
# http://localhost:8080
```

C) **Lokální MariaDB (DEMO profil):**
```bash
mvn clean spring-boot:run -Dspring-boot.run.profiles=demo
# http://localhost:8080
```
```
---

### Poznámky k Docker Compose
**Healthcheck DB:** Služba MariaDB má v `docker-compose.yml` nastavený `healthcheck`, takže aplikace startuje až ve chvíli, kdy je databáze skutečně připravená (méně chyb při prvním spuštění).

**UTF-8 konfigurace:** Konfigurační `.properties` soubory jsou uloženy v UTF-8 (bez BOM), aby správně fungovala diakritika napříč systémy a kontejnery.
