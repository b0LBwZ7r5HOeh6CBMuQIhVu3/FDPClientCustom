FILE=jbs.jar
if [ -f "$FILE" ]; then
    echo "file exists, rm"
    rm jbs.jar
fi
wget -O jbs.jar https://github.com/ll11l1lIllIl1lll/JByteStopper/releases/download/v1.0.2/JByteStopper-1.0.2.jar

echo "executing task1"
java -jar jbs.jar scripts/obf_resource.json
rm build/libs/FDPClient-2.0.0.jar
echo "executing task2"
java -jar jbs.jar scripts/obf_class.json
rm build/libs/FDPClient-2.0.0.jar.tmp
