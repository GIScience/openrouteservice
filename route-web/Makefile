build-image:
	docker build . -t benbergstein/route-web:latest

install:
	docker-compose -f ../docker/docker-compose.yml run client install

start:
	docker-compose -f ../docker/docker-compose.yml run -d --service-ports client

stop:
	docker-compose -f ../docker/docker-compose.yml stop client

