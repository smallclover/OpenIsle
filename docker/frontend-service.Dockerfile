FROM node:20

RUN apt-get update \
    && apt-get install -y --no-install-recommends rsync \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY docker/frontend-service-entrypoint.sh /usr/local/bin/frontend-service-entrypoint.sh
RUN chmod +x /usr/local/bin/frontend-service-entrypoint.sh

CMD ["frontend-service-entrypoint.sh"]
