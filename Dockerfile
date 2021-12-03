# Base image
FROM gradle:6.9.0-jdk11
# Set working directory so that all subsequent command runs in this folder
WORKDIR /prior-auth-client
# Copy app files to container
COPY --chown=gradle:gradle . .
# Embed CDS Library
# RUN gradle embedCdsLibrary
RUN gradle installBootDist
# Expose port to access the app
EXPOSE 9090
# Command to run our app
CMD gradle bootRun