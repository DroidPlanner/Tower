#!/bin/bash

if [ "$TRAVIS_REPO_SLUG" == "DroidPlanner/DroneKit-Android" ] && [ "$TRAVIS_JDK_VERSION" == "oraclejdk7" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "develop" ]; then

  echo -e "Publishing javadocs...\n"

  cp -R ClientLib/build/docs/javadoc $HOME/javadoc-latest

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/DroidPlanner/DroneKit-Android gh-pages > /dev/null

  cd gh-pages

  ## Clean and update javadoc
  git rm -rf .
  touch ./.nojekyll
  cp -Rf $HOME/javadoc-latest ./javadoc

  git add -f .
  git commit -m "Lastest documentation on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null

  echo -e "Published documentation to gh-pages.\n"
  
fi
