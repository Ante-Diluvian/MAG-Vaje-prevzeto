# Slika za namestitev backenda na Render. Vsebuje samo JRE in jar iz artefakta joba build,
# aplikacija se tu ne gradi ponovno. Kontekst gradnje je mapa s prenesenim jarom.
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY *.jar app.jar
EXPOSE 10000
# Render nastavi PORT (privzeto 10000). Zastavice JVM so za brezplačno instanco (512 MB, 0,1 CPU):
# omejijo pomnilnik in skrajšajo zagon.
ENTRYPOINT ["sh", "-c", "exec java -XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Dserver.port=${PORT:-8080} -jar app.jar"]
