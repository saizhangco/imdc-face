@echo

mvn clean package -DskipTests
xcopy ./target/imdc-face.jar ./ /s /e /y
java -jar imdc-face.jar