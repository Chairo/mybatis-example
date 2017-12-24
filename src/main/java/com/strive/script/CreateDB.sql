DROP TABLE subject;

CREATE TABLE subject (
  id     INT NOT NULL,
  name   VARCHAR(20),
  age    INT NOT NULL,
  height INT,
  weight INT
);

INSERT INTO subject (id, name, age, height, weight) VALUES (1, 'a', 10, 100, 45);
INSERT INTO subject (id, name, age, height, weight) VALUES (2, 'b', 10, NULL, 45);
INSERT INTO subject (id, name, age, height, weight) VALUES (2, 'b', 10, NULL, NULL);