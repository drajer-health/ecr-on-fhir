FROM node:14.15.4 as build

WORKDIR /usr/app

COPY package.json /usr/app

RUN npm install

COPY . /usr/app

RUN npm run build --prod

FROM nginx:1.17.1-alpine

RUN rm -rf /usr/share/nginx/html/*

COPY --from=build /usr/app/dist/eicr-responder-UI /usr/share/nginx/html

