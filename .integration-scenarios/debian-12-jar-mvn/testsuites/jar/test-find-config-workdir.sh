source $TESTFILES_DIR/testfunctions.sh

clearEnvironment

cd $REPO_DIR
cp $TESTFILES_DIR/config-car.yml ./ors-config.yml
java -jar $REPO_DIR/ors-api/target/ors.jar 1>/dev/null 2>&1 &

awaitOrsReady 30

profiles=$(requestEnabledProfiles)
#stopOrs
assertEquals "driving-car" "$profiles"
