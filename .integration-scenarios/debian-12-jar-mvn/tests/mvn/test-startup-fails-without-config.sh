source $TESTFILES_DIR/testfunctions.sh

clearEnvironment
# do not copy config to one of the default places

cd $WORK_DIR

# do not set params to enable a routing profile
mvn spring-boot:run -DskipTests 1>/dev/null 2>&1 &

# expect process finished within 100 sec
res=$(expectOrsStartupFails 60)

#stopOrs

assertEquals "terminated" "$res"
