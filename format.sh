#!/bin/bash

#Exit code :
# 0 work
# 1x google java format download error
# 10 fail google java format download
# 2x formating error
# 20 fail src/.../satoshiquest/ format
# 21 fail src/.../satoshiquest/commands/ format
# 22 fail src/.../satoshiquest/events/ format

#Setting pwd to our path
DIR=$0
if [[ $DIR != /* ]] ; then
  cd $(pwd)/$(dirname $0)
fi

# First step check if user have google format downloaded
if [ -e google-java-format-1.5-all-deps.jar ] ; then
  echo "google java format all ready downloaded"
else
  wget "https://github.com/google/google-java-format/releases/download/google-java-format-1.5/google-java-format-1.5-all-deps.jar"
  if [ $? != 0 ] ; then
    exit 10
  fi
fi

# google java format need to be executed with pwd satoshiquest/ or it will change import in a way to create compilation error.
chmod u+x google-java-format-1.5-all-deps.jar

java -jar google-java-format-1.5-all-deps.jar -r src/main/java/com/satoshiquest/satoshiquest/*.java
if [ $? != 0 ] ; then
  exit 20
fi
java -jar google-java-format-1.5-all-deps.jar -r src/main/java/com/satoshiquest/satoshiquest/commands/*.java
if [ $? != 0 ] ; then
  exit 21
fi
java -jar google-java-format-1.5-all-deps.jar -r src/main/java/com/satoshiquest/satoshiquest/events/*.java
if [ $? != 0 ] ; then
  exit 22
fi

# google java format need to be executed with pwd satoshiquest/ or it will change import in a way to create compilation error.
chmod u-x google-java-format-1.5-all-deps.jar

exit 0
