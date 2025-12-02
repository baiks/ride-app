FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
ARG NEXT_PUBLIC_API_BASE_URL
ARG APP_ENV=development  # Custom variable
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL
ENV APP_ENV=$APP_ENV
ENV NODE_ENV=production
RUN npm run build

FROM node:20-alpine
WORKDIR /app
COPY --from=build /app/.next ./.next
COPY --from=build /app/public ./public
COPY --from=build /app/package*.json ./
COPY --from=build /app/node_modules ./node_modules
ENV PORT=8076
ENV NODE_ENV=production
ENV APP_ENV=development
EXPOSE 8076
CMD ["npm", "start"]