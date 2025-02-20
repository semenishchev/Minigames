FROM ghcr.io/graalvm/native-image-community:23-muslib AS build

WORKDIR /workplace
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts ./
COPY build.gradle.kts ./
COPY assets ./assets
COPY api ./api
COPY minestom ./minestom

RUN ./gradlew build
RUN mkdir tmp && native-image -H:TempDirectory=/workdir/tmp --static --libc=musl -H:+UnlockExperimentalVMOptions -H:IncludeResources=".*." -Ob -jar minestom/build/libs/minestom-1.0-SNAPSHOT.jar -o minigame

FROM alpine:latest AS app
RUN adduser --disabled-password --home /home/container container
USER container
ENV  USER=container HOME=/home/containe
WORKDIR /home/container
COPY run/bedwars ./bedwars/
COPY --from=build /workplace/minigame ./
CMD ["/home/container/minigame"]
