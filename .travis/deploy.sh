#!/bin/bash
openssl aes-256-cbc -K $encrypted_8c043ad01516_key -iv $encrypted_8c043ad01516_iv -in .travis/deploy.key.enc -out .travis/deploy.key -d
chmod +x ./.travis/deploy.sh
eval "$(ssh-agent -s)"
chmod 600 .travis/deploy.key
ssh-add .travis/deploy.key
ssh-keyscan $DEPLOY_SERVER >> ~/.ssh/known_hosts
javadoc -d $HOME/javadoc-latest common/src/main/java/me/confuser/banmanager/common/api/BmAPI.java common/src/main/java/me/confuser/banmanager/common/data/*.java bukkit/src/main/java/me/confuser/banmanager/bukkit/api/events/*.java sponge/src/main/java/me/confuser/banmanager/sponge/api/events/*.java
cd $HOME/javadoc-latest
git init
git remote add deploy dokku@$DEPLOY_SERVER:javadocs.banmanagement.com >/dev/null 2>&1
git config --global push.default simple
git add -f .
git push -f deploy master >/dev/null 2>&1
