
echo. Do you really want to deploy this file?
pause
mvn deploy:deploy-file -DpomFile=pom.xml -Dfile=target/owls-api-3.1-SNAPSHOT.jar -DrepositoryId=uaal-thirdparty-snapshots -Durl=http://depot.universaal.org/maven-repo/thirdparty-snapshots/
pause
