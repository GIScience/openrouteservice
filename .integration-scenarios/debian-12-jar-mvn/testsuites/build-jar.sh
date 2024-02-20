source $TESTFILES_DIR/testfunctions.sh

clearEnvironment

cd $REPO_DIR
mvn clean package -DskipTests -PbuildFatJar
