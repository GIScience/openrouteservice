source $TESTFILES_DIR/testfunctions.sh

clearEnvironment
# do not copy config to one of the default places

cd $REPO_DIR

# do not set params to enable a routing profile
java -jar $REPO_DIR/ors-api/target/ors.jar 1>/dev/null 2>&1 &

# expect process finished within 100 sec
res=$(expectOrsStartupFails 100)

#stopOrs

assertEquals "terminated" "$res"
