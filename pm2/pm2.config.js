require('dotenv').config();
const VERSION = 'zbot-1.0.1';
console.log(process.env.DEFAULT_TOKEN_ZBT);
module.exports = {
  apps: [
    {
      name: 'z-bot-spring-boot',
      script: 'java',
      args: `-Ddefault.token=${process.env.DEFAULT_TOKEN_ZBT} -Ddefault.dev-mode=${process.env.DEFAULT_DEV_MODE_ZBT} -jar -Xmx512m -Xss512k ./target/${VERSION}.jar`,
    },
  ],
};