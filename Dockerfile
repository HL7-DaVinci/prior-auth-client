FROM gradle:jdk8-alpine
EXPOSE 9000/tcp
COPY --chown=gradle:gradle . /prior-auth-client/
WORKDIR /prior-auth-client/
RUN gradle install
CMD ["gradle", "run"]
