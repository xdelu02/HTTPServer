DROP DATABASE IF EXISTS HTTPServer;
CREATE DATABASE HTTPServer;

USE HTTPServer;

CREATE TABLE Persone(
	id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    nome varchar(45),
    cognome varchar(45)
);

INSERT INTO Persone (nome, cognome)
VALUES ("Mario", "Rossi"),
	   ("Giovanni", "Verdi"),
       ("Luigi", "Bianchi"),
       ("Marco", "Gialli"),
       ("Giorgio", "Violi"),
       ("Lorenzo", "Arancioni"),
       ("Luciano", "Blu"),
       ("Francesco", "Rosi"),
       ("Tommaso", "Marroni"),
       ("Filippo", "Neri");