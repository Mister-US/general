FROM openjdk:8-jdk

WORKDIR /opt

RUN mkdir -p /opt/input

ADD vertxeventbussender-1.0.0-SNAPSHOT-fat.jar /opt

VOLUME /opt/input

CMD java -jar /opt/vertxeventbussender-1.0.0-SNAPSHOT-fat.jar '/opt/input'
