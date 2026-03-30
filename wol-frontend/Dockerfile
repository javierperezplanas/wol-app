# Etapa 1: Construcción
FROM node:20-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build -- --configuration production

# Etapa 2: Servidor Nginx ligero
FROM nginx:stable-alpine
# Ojo: comprueba si tu carpeta de salida es /dist/wol-frontend/browser o solo /dist/wol-frontend
COPY --from=build /app/dist/wol-frontend/browser /usr/share/nginx/html
COPY wol-nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
