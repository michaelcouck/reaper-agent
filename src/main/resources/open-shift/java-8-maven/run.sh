#! /bin/bash -e

# rm -rf $SOURCE_DIRECTORY
# mkdir -p $SOURCE_DIRECTORY
cd $SOURCE_DIRECTORY
chmod 777 -R $SOURCE_DIRECTORY

echo $SOURCE_CODE
echo $SOURCE_DIRECTORY

# Clone the source into the directory for building
git clone $SOURCE_CODE $SOURCE_DIRECTORY
mvn install

# chmod 777 -R /root/source

# Finds the first jar in the maven target build directory and execute it 
echo 'Working directory'
pwd
echo 'List source directory'
ls -l
echo 'List target directory'
ls -l target

echo find target -maxdepth 1 -name "*.jar"
echo 'Executing application artifact'
find target -maxdepth 1 -name "*.jar" | xargs -n1 java -jar
