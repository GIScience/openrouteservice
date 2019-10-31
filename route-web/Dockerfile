FROM node:latest

WORKDIR /app
ADD package.json /app/package.json
RUN yarn

ADD . /app

ENTRYPOINT ["yarn"]
CMD ["start"]
