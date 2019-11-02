build-image:
	docker build . -t benbergstein/route-web:latest

install:
	docker-compose -f ../docker/docker-compose.yml run client install

start:
	docker-compose -f ../docker/docker-compose.yml run -d --service-ports client

serve:
	docker-compose -f ../docker/docker-compose.yml run -d -p "80:5000" client serve

stop:
	docker-compose -f ../docker/docker-compose.yml stop client

build:
	docker-compose -f ../docker/docker-compose.yml run client build
