# Family Clock
This is a web based family clock inspired by a clock featured in a
very famous book (and movie) series about wizards.  Each family
member is assigned a hand and the hand points to a location.

## Building

## Configuring

## Running the Server as a Docker Containter
This is designed to run as a war using the [Unidata hardened Tomcat server](https://github.com/Unidata/tomcat-docker).

Presume you're running under some kind of Linux, you installed using rpm, you've used Let's Encrypt 
to create your ssl key, you're binding to the standard http and https ports, and
you have a database set up some place.  Start it like this:

````bash
docker run -it -d \
-p 80:8080 \
-p 443:8443 \
-v /usr/share/tomcat/family-clock-war-1.0-SNAPSHOT.war:/usr/local/tomcat/webapps/ROOT.war \
-v /usr/share/tomcat/context.xml:/usr/local/tomcat/conf/context.xml \
-v /usr/share/tomcat/tomcat/server.xml:/usr/local/tomcat/conf/server.xml \
-v /usr/share/tomcat/tomcat/catalina.policy:/usr/local/tomcat/conf/catalina.policy \
-v /home/ec2-user/tomcat/logs:/usr/local/tomcat/logs \
-v /etc/letsencrypt/live/familyclock.sample.com/fullchain.pem:/usr/local/tomcat/conf/ssl.crt \
-v /etc/letsencrypt/live/familyclock.sample.com/privkey.pem:/usr/local/tomcat/conf/ssl.key \
-e TOMCAT_USER_ID=`id -u` \
-e TOMCAT_GROUP_ID=`getent group $USER | cut -d':' -f3` \
-e "JAVA_OPTS=\
-Ddb.user=dbuser \
-Ddb.pwd=dbpassword \
-Ddb.url=jdbc:mysql://10.1.2.3:3306/familyclock \
-Drest.user=owntracks \
-Drest.password=OwnTracksPwd \
-Dhttps.port.redirect=443" \
--name family-clock \
unidata/tomcat-docker:8

````
