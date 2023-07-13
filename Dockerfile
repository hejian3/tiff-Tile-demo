from openjdk:11
VOLUME /tmp
ENV TZ=Asia/Shanghai
ARG JAR_FILE=target/tiff-Tile-demo-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} /jars/tiff-Tile-demo.jar
COPY 202307061316291.tiff /root
RUN mkdir /root/gdal_test && ln -snf /usr/share/zoneinfo/$TZ  /etc/localtime && echo $TZ > /etc/timezon
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/jars/tiff-Tile-demo.jar"]