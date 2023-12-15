git stash && git pull


if mvn clean install; then
  echo "updated"
else
  echo "ensure you are running this command from the root of the project"
fi

pm2 delete z-bot-spring-boot;

bash pm2/pm2-starter.bash