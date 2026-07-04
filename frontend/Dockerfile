FROM node:20-alpine AS build
WORKDIR /workspace

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:1.27-alpine
ENV API_UPSTREAM=http://api-gateway:8080
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /workspace/dist/rallo-frontend/browser /usr/share/nginx/html
EXPOSE 80
