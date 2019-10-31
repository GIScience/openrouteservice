build-image:
	docker build . -t benbergstein/route-web:latest

install:
	docker-compose -f ../docker/docker-compose.yml run client install

start:
	docker-compose -f ../docker/docker-compose.yml run -d client
stop:
	docker-compose -f ../docker/docker-compose.yml down client --remove-orphans

