// NOTE: This script should be run from the root directory
// example: pm2 start pm2/pm2.config.js -- --version 1.2.0
require('dotenv').config();

function getVersion(...flags) {
  for (const name of flags) {
    const index = process.argv.indexOf(name);
    if (index !== -1) {
      return process.argv[index + 1];
    }
  }
}

const version = getVersion('--v', '--version');
if (!version) throw new Error('Expected version number');

module.exports = {
  apps: [
    {
      name: 'z-bot-spring-boot',
      script: 'java',
      args: `-Ddefault.token=${process.env.DEFAULT_TOKEN_ZBT} -Dspring.profiles.active=prod -jar -Xmx512m -Xss512k ./target/zbot-${version}.jar`,
    },
  ],
};