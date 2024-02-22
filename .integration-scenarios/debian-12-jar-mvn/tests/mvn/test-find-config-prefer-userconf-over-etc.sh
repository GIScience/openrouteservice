source $TESTFILES_DIR/testfunctions.sh

clearEnvironment

cp $TESTFILES_DIR/config-car.yml $CONF_DIR_USER/ors-config.yml
cp $TESTFILES_DIR/config-hgv.yml $CONF_DIR_ETC/ors-config.yml

cd $WORK_DIR
mvn spring-boot:run -DskipTests &

awaitOrsReady 60 $HOST_PORT

profiles=$(requestEnabledProfiles $HOST_PORT)
#stopOrs
assertEquals "driving-car" "$profiles"
