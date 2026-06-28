.PHONY: build up down test clean

build:
	mvn package -DskipTests -B

up: build
	docker compose up --build -d

down:
	docker compose down

test:
	mvn verify -B

clean:
	mvn clean -B
	docker compose down -v
