#!/bin/sh
echo "----------------------------------------------"
echo "      starting ml worker"
echo "----------------------------------------------"
cd /tmp
rm -rf user-data*
rm bigs-worker.log
rm bigs-worker.err
wget http://169.254.169.254/latest/user-data
echo "launching bigs --config user-data worker"
echo "------------ config file ---------------------"
cat user-data
export JAVA_HOME=/opt/java
/opt/bigs/bin/bigs --config user-data worker > /tmp/bigs-worker.log 2> /tmp/bigs-worker.err &
echo "----------------------------------------------"

