
build:
	lein clean
	lein uberjar

run:
	lein run

database-up:
	docker compose up

database-down:
	docker compose down

kill-db-port:
	sudo fuser -k 5432/tcp