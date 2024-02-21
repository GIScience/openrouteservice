source $TESTFILES_DIR/testfunctions.sh

clearEnvironment

cd $WORK_DIR
mvn clean package -DskipTests -PbuildFatJar
