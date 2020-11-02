FROM openjdk:8
LABEL MAINTAINER="TheHive Project <support@thehive-project.org>"
WORKDIR /opt/cortex
ADD --chown=daemon:daemon opt /opt
USER root
RUN ["bash", "-c", "wget -q -O - https://download.docker.com/linux/static/stable/x86_64/docker-18.09.0.tgz | tar -xzC /usr/local/bin/ --strip-components 1 && addgroup --system dockremap && adduser --system --ingroup dockremap dockremap && addgroup --system docker && usermod --append --groups docker daemon &&echo 'dockremap:165536:65536' >> /etc/subuid && echo 'dockremap:165536:65536' >> /etc/subgid && apt-get update && apt-get upgrade -y && apt-get install -y --no-install-recommends python-pip python2.7-dev python3-pip python3-dev ssdeep libfuzzy-dev libfuzzy2 libimage-exiftool-perl libmagic1 build-essential git libssl-dev dnsutils iptables && pip2 install -U pip setuptools && pip3 install -U pip setuptools && hash -r && cd /opt && git clone https://github.com/TheHive-Project/Cortex-Analyzers.git && for I in $(find Cortex-Analyzers -name 'requirements.txt'); do pip2 install -r $I; done && for I in $(find Cortex-Analyzers -name 'requirements.txt'); do pip3 install -r $I || true; done"]
ADD var /var
ADD etc /etc
RUN ["chown", "-R", "daemon:root", "/var/log/cortex"]
RUN ["chmod", "+x", "/opt/cortex/bin/cortex", "/opt/cortex/entrypoint"]
EXPOSE 9001
ENTRYPOINT ["/opt/cortex/entrypoint"]
CMD []
