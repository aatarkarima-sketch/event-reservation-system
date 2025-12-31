#  Event Reservation System - Système de Gestion de Réservations d'Événements

Application web complète pour la gestion de réservations d'événements culturels (concerts, théâtres, conférences, sports) développée avec **Java 17**, **Spring Boot 3.x** et **Vaadin 24.x**.

---

##  Table des Matières

- [Technologies Utilisées](#technologies-utilisées)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Lancement de l'Application](#lancement-de-lapplication)
- [Accès à l'Application](#accès-à-lapplication)
- [Comptes de Test](#comptes-de-test)
- [Fonctionnalités](#fonctionnalités)
- [Architecture](#architecture)
- [Structure du Projet](#structure-du-projet)
- [Tests](#tests)
- [Dépannage](#dépannage)

---

##  Technologies Utilisées

- **Java**: 17+
- **Spring Boot**: 3.2.0
- **Spring Data JPA**: Gestion de la persistance
- **Spring Security**: Authentification et autorisation
- **Vaadin**: 24.3.0 (Framework UI Java)
- **H2 Database**: Base de données embarquée
- **Lombok**: Réduction du code boilerplate
- **Maven**: Gestion des dépendances et build

---

##  Prérequis

Avant de commencer, assurez-vous d'avoir installé :

- **JDK 17 ou supérieur** : [Télécharger Java](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** : [Télécharger Maven](https://maven.apache.org/download.cgi)
- **IDE Java** (recommandé) :
    - IntelliJ IDEA
    - Eclipse
    - VS Code avec Extension Pack for Java

### Vérifier les installations

```bash
# Vérifier Java
java -version

# Vérifier Maven
mvn -version
```

---

##  Installation

### 1. Cloner le Projet

```bash
git clone <url-du-repository>
cd event-reservation-system
```

### 2. Construire le Projet

```bash
mvn clean install
```

Cette commande va :
- Télécharger toutes les dépendances
- Compiler le code
- Exécuter les tests
- Créer le fichier JAR exécutable

---

##  Configuration

### Base de Données H2

La configuration par défaut utilise H2 en mode mémoire. Aucune configuration supplémentaire n'est nécessaire.

Le fichier `application.properties` contient :

```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:eventdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Console H2 (pour debug)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Données Initiales

Le fichier `data.sql` contient des données de test qui seront automatiquement chargées au démarrage :
- 5 utilisateurs (1 admin, 2 organisateurs, 2 clients)
- 15 événements variés
- 20 réservations

---

##  Lancement de l'Application

### Option 1 : Via Maven

```bash
mvn spring-boot:run
```

### Option 2 : Via le JAR

```bash
# Construire le JAR
mvn clean package

# Exécuter le JAR
java -jar target/event-reservation-system-1.0.0.jar
```

### Option 3 : Via IDE

1. Ouvrir le projet dans votre IDE
2. Localiser la classe principale : `EventReservationSystemApplication.java`
3. Exécuter avec `Run` ou `Debug`

---

## Accès à l'Application

Une fois l'application démarrée :

- **Application Web** : [http://localhost:8080](http://localhost:8080)
- **Console H2** : [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
    - JDBC URL : `jdbc:h2:mem:eventdb`
    - Username : `sa`
    - Password : *(vide)*

---

##  Comptes de Test

### Administrateur
- **Email** : `admin@event.ma`
- **Mot de passe** : `Password123`
- **Rôle** : Accès complet à toutes les fonctionnalités

### Organisateurs
- **Email** : `organizer1@event.ma` ou `organizer2@event.ma`
- **Mot de passe** : `Password123`
- **Rôle** : Création et gestion d'événements

### Clients
- **Email** : `client1@event.ma` ou `client2@event.ma`
- **Mot de passe** : `Password123`
- **Rôle** : Réservation d'événements

---

## Fonctionnalités

### Pour Tous les Utilisateurs (Non Connectés)
- ✅ Consulter la liste des événements disponibles
- ✅ Rechercher et filtrer les événements
- ✅ Voir les détails d'un événement
- ✅ S'inscrire et se connecter

### Pour les Clients
- ✅ Réserver des places pour des événements
- ✅ Gérer ses réservations
- ✅ Annuler une réservation (jusqu'à 48h avant)
- ✅ Voir l'historique des réservations
- ✅ Mettre à jour son profil

### Pour les Organisateurs
- ✅ Créer des événements
- ✅ Modifier ses événements
- ✅ Publier/Annuler des événements
- ✅ Voir les réservations par événement
- ✅ Toutes les fonctionnalités client

### Pour les Administrateurs
- ✅ Gérer tous les utilisateurs
- ✅ Gérer tous les événements
- ✅ Voir toutes les réservations
- ✅ Accès aux statistiques globales
- ✅ Toutes les fonctionnalités

---

## Architecture

### Architecture en Couches

```
┌─────────────────────────────────────┐
│         Vaadin Views (UI)           │
│    (HomeView, LoginView, etc.)      │
├─────────────────────────────────────┤
│         Services Layer              │
│  (UserService, EventService, etc.)  │
├─────────────────────────────────────┤
│      Repositories Layer             │
│ (UserRepository, EventRepository)   │
├─────────────────────────────────────┤
│          JPA/Hibernate              │
├─────────────────────────────────────┤
│         H2 Database                 │
└─────────────────────────────────────┘
```

### Design Patterns Utilisés

- **MVC** : Séparation Model-View-Controller
- **Repository Pattern** : Abstraction de la couche données
- **Service Layer** : Logique métier
- **Dependency Injection** : Inversion de contrôle avec Spring
- **Builder Pattern** : Construction d'objets complexes (Lombok)
- **Observer Pattern** : Événements Vaadin

---


```

---

##  Tests

### Exécuter Tous les Tests

```bash
mvn test
```

### Exécuter un Test Spécifique

```bash
mvn test -Dtest=UserServiceTest
```

### Couverture de Code

```bash
mvn clean verify
```

---

##  Dépannage

### Problème : Port 8080 déjà utilisé

**Solution** : Modifier le port dans `application.properties`

```properties
server.port=8081
```

### Problème : Erreur de compilation Maven

**Solution** :

```bash
mvn clean
mvn install -U
```

### Problème : Base de données H2 non accessible

**Vérifications** :
1. Console H2 activée dans `application.properties`
2. URL correcte : `jdbc:h2:mem:eventdb`
3. Utilisateur : `sa`, mot de passe vide

### Problème : Données initiales non chargées

**Solution** : Vérifier que :

```properties
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
```

---

## Concepts Java Avancés Utilisés

### Streams API
- Filtrage et transformation des collections
- `map()`, `filter()`, `collect()`, `reduce()`
- Calculs statistiques avec `Collectors.groupingBy()`

### Optional
- Gestion sécurisée des valeurs nulles
- Chaînage avec `map()`, `flatMap()`, `orElse()`
- Éviter les `NullPointerException`

### Lambda Expressions
- Listeners Vaadin
- Comparateurs personnalisés
- Interfaces fonctionnelles (`Predicate`, `Function`, `Supplier`)

### Generics
- Repositories génériques
- Méthodes utilitaires réutilisables

### Enums avec Méthodes
- Logique métier dans les enums
- Méthodes `getLabel()`, `getColor()`, `getIcon()`

---

##  Règles Métier Importantes

1. **Réservations** :
    - Maximum 10 places par réservation
    - Annulation possible jusqu'à 48h avant l'événement
    - Code unique au format `EVT-XXXXX`

2. **Événements** :
    - Dates dans le futur obligatoires
    - Publication uniquement si informations complètes
    - Suppression impossible si réservations actives

3. **Utilisateurs** :
    - Mot de passe : min 8 caractères, majuscule, minuscule, chiffre
    - Email unique
    - Rôles : ADMIN, ORGANIZER, CLIENT

---

##  Améliorations Futures

- [ ] Upload d'images pour les événements
- [ ] Notifications par email
- [ ] Système de paiement en ligne
- [ ] Export PDF des billets
- [ ] Intégration Google Maps
- [ ] Application mobile (Flutter/React Native)
- [ ] Mode sombre/clair
- [ ] Système d'avis et commentaires

---

## 👨‍💻 Auteur


- Email: aatarkaima@gmail.com 
        - jettiuoimaroua@gmail.com

---



Ce projet est développé dans le cadre d'un projet académique.

---



**Date de création** : Décembre 2025
