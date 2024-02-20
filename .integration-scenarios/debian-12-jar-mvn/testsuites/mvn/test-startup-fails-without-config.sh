source $TESTFILES_DIR/testfunctions.sh

clearEnvironment
# do not copy config to one of the default places

cd $REPO_DIR

# do not set params to enable a routing profile
mvn spring-boot:run -DskipTests &

# expect process finished within 100 sec
res=$(expectOrsStartupFails 30)

#stopOrs

assertEquals "terminated" "$res"
