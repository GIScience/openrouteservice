source $TESTFILES_DIR/testfunctions.sh

clearEnvironment

cp $TESTFILES_DIR/config-car.yml $CONF_DIR_ETC/ors-config.yml

cd $WORK_DIR
java -jar $WORK_DIR/ors-api/target/ors.jar 1>/dev/null 2>&1 &

awaitOrsReady 30

profiles=$(requestEnabledProfiles)
#stopOrs
assertEquals "driving-car" "$profiles"
