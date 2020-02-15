FROM debian:stable
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update
RUN apt-get install -y software-properties-common dirmngr maven git build-essential gnupg default-jre default-jdk
# RUN echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list
# RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886  && \
# RUN apt-get update
# RUN echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
# RUN DEBIAN_FRONTEND=noninteractive  apt-get install -y oracle-java8-installer oracle-java8-set-default


RUN mkdir -p /spigot/plugins
WORKDIR /build
# DOWNLOAD AND BUILD SPIGOT
ADD https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar /build/BuildTools.jar
RUN cd /build && java -jar BuildTools.jar --rev 1.13.2
RUN cp /build/Spigot/Spigot-Server/target/spigot-*.jar /spigot/spigot.jar
WORKDIR /spigot
RUN echo "eula=true" > eula.txt
COPY server.properties /spigot/
COPY bukkit.yml /spigot/
COPY spigot.yml /spigot/
WORKDIR /satoshiquest
COPY . /satoshiquest/
RUN mvn clean compile assembly:single
RUN cp /satoshiquest/target/SatoshiQuest.jar /spigot/plugins/SatoshiQuest.jar
# Add the last version of NoCheatPlus
# ADD https://github.com/BitcoinJake09/SatoshiQuest/blob/master/Clearlag.jar /spigot/plugins/Clearlag.jar
WORKDIR /spigot
CMD java -Xmx8192m -jar spigot.jar
