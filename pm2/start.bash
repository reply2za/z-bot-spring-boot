git stash && git pull && mvn clean install

pm2 delete z-bot-spring-boot;

bash pm2/pm2-starter.bash