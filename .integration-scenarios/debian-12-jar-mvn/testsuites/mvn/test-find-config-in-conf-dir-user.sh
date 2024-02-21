source $TESTFILES_DIR/testfunctions.sh

clearEnvironment

cp $TESTFILES_DIR/config-car.yml $CONF_DIR_USER/ors-config.yml

cd $WORK_DIR
mvn spring-boot:run -DskipTests &

awaitOrsReady 30

profiles=$(requestEnabledProfiles)
#stopOrs
assertEquals "driving-car" "$profiles"
