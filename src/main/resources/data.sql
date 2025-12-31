-- Fichier: src/main/resources/data.sql
-- Données initiales pour le système de gestion de réservations d'événements

-- ====================
-- UTILISATEURS
-- ====================
-- Mot de passe pour tous: Password123
-- Hash BCrypt de "Password123"
INSERT INTO users (nom, prenom, email, password, role, date_inscription, actif, telephone) VALUES
('Admin', 'System', 'admin@event.ma', '$2a$10$YtpsBAD/45QU/2EHP9uBTOQAMa6EFvhft01nuJLdvLxCFvoYDsO0q', 'ADMIN', CURRENT_TIMESTAMP, true, '+212600000001'),
('Alami', 'Hassan', 'organizer1@event.ma', '$2a$10$YtpsBAD/45QU/2EHP9uBTOQAMa6EFvhft01nuJLdvLxCFvoYDsO0q', 'ORGANIZER', CURRENT_TIMESTAMP, true, '+212600000002'),
('Benani', 'Fatima', 'organizer2@event.ma', '$2a$10$YtpsBAD/45QU/2EHP9uBTOQAMa6EFvhft01nuJLdvLxCFvoYDsO0q', 'ORGANIZER', CURRENT_TIMESTAMP, true, '+212600000003'),
('Tazi', 'Mohamed', 'client1@event.ma', '$2a$10$YtpsBAD/45QU/2EHP9uBTOQAMa6EFvhft01nuJLdvLxCFvoYDsO0q', 'CLIENT', CURRENT_TIMESTAMP, true, '+212600000004'),
('Idrissi', 'Amina', 'client2@event.ma', '$2a$10$YtpsBAD/45QU/2EHP9uBTOQAMa6EFvhft01nuJLdvLxCFvoYDsO0q', 'CLIENT', CURRENT_TIMESTAMP, true, '+212600000005');

-- ====================
-- ÉVÉNEMENTS - CONCERTS
-- ====================
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, organisateur_id, statut, date_creation, date_modification, image_url) VALUES
('Festival Gnaoua World Music', 'Le plus grand festival de musique Gnaoua au monde. Découvrez les rythmes envoûtants de la musique traditionnelle marocaine.', 'CONCERT', '2025-06-20 19:00:00', '2025-06-20 23:00:00', 'Place Moulay Hassan', 'Essaouira', 5000, 200.0, 2, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null),
('Concert Saad Lamjarred', 'Grande soirée avec la star marocaine Saad Lamjarred. Un spectacle inoubliable !', 'CONCERT', '2026-02-15 20:00:00', '2026-02-15 23:30:00', 'Stade Mohammed V', 'Casablanca', 3000, 300.0, 2, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null),
('Nuit du Raï', 'Festival de musique Raï avec les plus grandes stars algériennes et marocaines.', 'CONCERT', '2026-03-10 21:00:00', '2026-03-11 02:00:00', 'Complexe Culturel Mohammed VI', 'Oujda', 2000, 150.0, 3, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null);

-- ====================
-- ÉVÉNEMENTS - THÉÂTRE
-- ====================
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, organisateur_id, statut, date_creation, date_modification, image_url) VALUES
('Le Roi Lear - Version Marocaine', 'Adaptation marocaine du classique de Shakespeare. Mise en scène exceptionnelle par Tayeb Saddiki.', 'THEATRE', '2025-02-01 20:00:00', '2025-02-01 22:30:00', 'Théâtre Mohammed V', 'Rabat', 500, 120.0, 2, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null),
('Comédie Marocaine: Lmima', 'Une comédie familiale marocaine qui vous fera rire aux éclats !', 'THEATRE', '2026-01-25 19:30:00', '2026-01-25 21:30:00', 'Théâtre Royal', 'Marrakech', 400, 100.0, 3, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null),
('Molière en Darija', 'Le Bourgeois Gentilhomme adapté en dialecte marocain.', 'THEATRE', '2026-03-05 20:00:00', '2026-03-05 22:00:00', 'Institut Français', 'Tanger', 300, 80.0, 2, 'BROUILLON', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null);

-- ====================
-- ÉVÉNEMENTS - CONFÉRENCES
-- ====================
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, organisateur_id, statut, date_creation, date_modification, image_url) VALUES
('TEDx Casablanca 2025', 'Conférence TED avec les meilleurs speakers marocains et internationaux. Thème: Innovation et Développement Durable.', 'CONFERENCE', '2025-04-15 09:00:00', '2025-04-15 18:00:00', 'Technopark', 'Casablanca', 800, 250.0, 2, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null),
('Sommet de l''Entrepreneuriat', 'Rencontrez les leaders de l''écosystème startup marocain. Networking et opportunités d''affaires.', 'CONFERENCE', '2026-05-20 10:00:00', '2026-05-20 17:00:00', 'Palais des Congrès', 'Marrakech', 1000, 350.0, 3, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null),
('Forum Digital Morocco', 'Conférence sur la transformation digitale au Maroc. Experts internationaux et locaux.', 'CONFERENCE', '2026-06-10 09:00:00', '2026-06-10 16:00:00', 'Sofitel', 'Rabat', 600, 200.0, 2, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null);

-- ====================
-- ÉVÉNEMENTS - SPORT
-- ====================
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, organisateur_id, statut, date_creation, date_modification, image_url) VALUES
('Raja vs Wydad - Derby de Casablanca', 'Le derby le plus chaud d''Afrique ! Ne manquez pas ce match historique.', 'SPORT', '2025-03-15 20:00:00', '2025-03-15 22:00:00', 'Stade Mohammed V', 'Casablanca', 45000, 150.0, 3, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null),
('Marathon International de Marrakech', 'Participez au plus grand marathon du Maroc. 42km à travers la ville rouge.', 'SPORT', '2025-01-27 07:00:00', '2025-01-27 13:00:00', 'Avenue Mohammed VI', 'Marrakech', 5000, 200.0, 2, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null),
('Tournoi de Tennis Hassan II', 'Tournoi ATP avec les plus grands joueurs mondiaux. Ambiance exceptionnelle.', 'SPORT', '2025-04-05 10:00:00', '2025-04-05 18:00:00', 'Royal Tennis Club', 'Casablanca', 2000, 400.0, 3, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null);

-- ====================
-- ÉVÉNEMENTS - AUTRES
-- ====================
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, organisateur_id, statut, date_creation, date_modification, image_url) VALUES
('Salon du Livre de Casablanca', 'Le plus grand salon du livre au Maroc. Rencontres avec les auteurs, dédicaces, conférences.', 'AUTRE', '2025-02-10 10:00:00', '2025-02-20 20:00:00', 'Foire Internationale', 'Casablanca', 10000, 50.0, 2, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null),
('Festival de Cinéma de Marrakech', 'Projection des meilleurs films marocains et internationaux. Tapis rouge et célébrités.', 'AUTRE', '2025-12-01 18:00:00', '2025-12-10 23:00:00', 'Palais des Congrès', 'Marrakech', 3000, 180.0, 3, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null),
('Fête de la Musique Tanger', 'Célébrez la musique dans toute la ville ! Concerts gratuits et payants dans différents lieux.', 'AUTRE', '2025-06-21 17:00:00', '2025-06-21 23:00:00', 'Centre Ville', 'Tanger', 8000, 0.0, 2, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, null);

-- ====================
-- RÉSERVATIONS
-- ====================
INSERT INTO reservations (utilisateur_id, evenement_id, nombre_places, montant_total, date_reservation, statut, code_reservation, commentaire) VALUES
-- Réservations pour le client 1 (Mohamed Tazi)
(4, 1, 2, 400.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10001', 'Réservation pour moi et ma femme'),
(4, 2, 3, 900.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10002', 'Réservation familiale'),
(4, 7, 1, 250.0, CURRENT_TIMESTAMP, 'EN_ATTENTE', 'EVT-10003', null),
(4, 10, 4, 600.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10004', 'Groupe d''amis'),
(4, 13, 2, 100.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10005', null),

-- Réservations pour le client 2 (Amina Idrissi)
(5, 1, 4, 800.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10006', 'Sortie entre amies'),
(5, 4, 2, 240.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10007', null),
(5, 8, 5, 1750.0, CURRENT_TIMESTAMP, 'EN_ATTENTE', 'EVT-10008', 'Groupe entreprise'),
(5, 11, 2, 800.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10009', null),
(5, 14, 1, 180.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10010', 'Passionnée de cinéma'),

-- Réservations supplémentaires
(4, 3, 2, 300.0, CURRENT_TIMESTAMP, 'ANNULEE', 'EVT-10011', 'Changement de plans'),
(5, 5, 3, 300.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10012', 'Théâtre en famille'),
(4, 9, 6, 900.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10013', 'Dossard pour mes enfants'),
(5, 12, 1, 400.0, CURRENT_TIMESTAMP, 'EN_ATTENTE', 'EVT-10014', null),
(4, 15, 10, 0.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10015', 'Événement gratuit'),

-- Réservations pour événements à forte demande
(4, 10, 5, 750.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10016', 'Match important'),
(5, 10, 3, 450.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10017', 'Derby passionnant'),
(4, 2, 2, 600.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10018', 'Fan de Saad'),
(5, 7, 2, 500.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10019', 'TEDx motivation'),
(4, 8, 3, 1050.0, CURRENT_TIMESTAMP, 'CONFIRMEE', 'EVT-10020', 'Networking business');

-- Mise à jour des URLs d'images (exécute ça après les INSERT ou remplace directement)
UPDATE events SET image_url = '/images/fistivale.jpg' WHERE titre LIKE '%Gnaoua%'; -- Festival musique traditionnelle
UPDATE events SET image_url = 'https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800&q=80' WHERE titre LIKE '%Saad Lamjarred%'; -- Concert pop
UPDATE events SET image_url = '/images/hero.jpg' WHERE titre LIKE '%Nuit du Raï%'; -- Concert raï

UPDATE events SET image_url = '/images/theatre.jpg' WHERE titre LIKE '%Roi Lear%'; -- Théâtre classique
UPDATE events SET image_url = '/images/comodie.webp' WHERE titre LIKE '%Lmima%'; -- Comédie
UPDATE events SET image_url = 'https://images.unsplash.com/photo-1518562917821-8557f2c8b91d?w=800&q=80' WHERE titre LIKE '%Molière%';

UPDATE events SET image_url = '/images/Corporate Event Creativity - conferences.jpg' WHERE titre LIKE '%TEDx%'; -- Conférence
UPDATE events SET image_url = 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&q=80' WHERE titre LIKE '%Sommet%'; -- Conférence business
UPDATE events SET image_url = 'https://images.unsplash.com/photo-1505373877841-8d25f771e95b?w=800&q=80' WHERE titre LIKE '%Digital%';

UPDATE events SET image_url = '/images/Sport.jpg' WHERE titre LIKE '%Derby%'; -- Football
UPDATE events SET image_url = '/images/sportevent.jpg' WHERE titre LIKE '%Marathon%'; -- Course
UPDATE events SET image_url = 'https://images.unsplash.com/photo-1575361208123-2f9b8f3d0f9f?w=800&q=80' WHERE titre LIKE '%Tennis%';

UPDATE events SET image_url = 'https://images.unsplash.com/photo-1543002588-b1a8641d9ac8?w=800&q=80' WHERE titre LIKE '%Salon du Livre%'; -- Livre
UPDATE events SET image_url = 'https://images.unsplash.com/photo-1536440136628-849c177e375a?w=800&q=80' WHERE titre LIKE '%Cinéma%'; -- Festival cinéma
UPDATE events SET image_url = 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800&q=80' WHERE titre LIKE '%Fête de la Musique%'; -- Musique gratuite