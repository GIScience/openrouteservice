source $TESTFILES_DIR/testfunctions.sh

clearEnvironment

cd $REPO_DIR
cp $TESTFILES_DIR/config-car.yml ./ors-config.yml
mvn spring-boot:run -DskipTests &

awaitOrsReady 30

profiles=$(requestEnabledProfiles)
#stopOrs
assertEquals "driving-car" "$profiles"
