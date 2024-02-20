source $TESTFILES_DIR/testfunctions.sh

clearEnvironment

cd $REPO_DIR
cp $TESTFILES_DIR/config-car-hgv-whe.yml ./ors-config.yml
mvn spring-boot:run -DskipTests &

awaitOrsReady 300

stopOrs
