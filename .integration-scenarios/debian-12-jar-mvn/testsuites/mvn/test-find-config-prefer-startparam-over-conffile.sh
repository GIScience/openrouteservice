source $TESTFILES_DIR/testfunctions.sh

clearEnvironment

cp $TESTFILES_DIR/config-car.yml $WORK_DIR/ors-config.yml

cd $WORK_DIR
mvn spring-boot:run -Dspring-boot.run.arguments=$TESTFILES_DIR/config-hgv.yml & # 1>/dev/null 2>&1 &

awaitOrsReady 30

profiles=$(requestEnabledProfiles)
#stopOrs
assertEquals "driving-hgv" "$profiles"
