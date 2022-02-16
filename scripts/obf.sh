FILE=jbs.jar
if [ -f "$FILE" ]; then
    echo "file exists, skipping download"
else
    wget -O obf.jar https://github.com/UnlegitMC/JByteStopper/releases/download/v1.0.1/JByteStopper-1.0.1.jar
fi

echo "executing task1"
java -jar jbs.jar scripts/obf_resource.json
rm build/libs/FDPClient-2.0.0.jar
echo "executing task2"
java -jar jbs.jar scripts/obf_class.json
rm build/libs/FDPClient-2.0.0.jar.tmp
