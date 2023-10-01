git pull && mvn clean install && pm2 delete z-bot-spring-boot

pm2 start pm2/pm2.config.js -- --version 1.2.0