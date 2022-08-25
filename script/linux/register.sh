if [ "$(whoami)" != "root" ]
then
    echo "请使用root账号启动";
    exit 1;
fi

SCRIPT_PATH="${BASH_SOURCE[0]}"
SCRIPT_DIR=`dirname ${SCRIPT_PATH}`
BASE_DIR=`cd ${SCRIPT_DIR} && pwd`
PROJ_DIR=`cd ${BASE_DIR}/.. && pwd`
DATA_DIR="${BASE_DIR}/data"
PROJ_NAME=`basename ${BASE_DIR}`

JAR_FILE="${BASE_DIR}/${PROJ_NAME}.jar"
TEMP_DIR="${BASE_DIR}/temp"
LOG_DIR="${BASE_DIR}/logs"

mkdir -p ${TEMP_DIR}
mkdir -p ${LOG_DIR}

echo "Temporary directory: ${TEMP_DIR}"
echo "Logging path: ${LOG_DIR}"
echo "Project JAR: ${JAR_FILE}"

(LOG_FILE_DIR="${LOG_DIR}" APP_DATA_DIR=${DATA_DIR} \
${BASE_DIR}/jre/bin/java -Xms512m -Xmx1024m \
    -Djava.security.egd=file:/dev/./urandom \
    -Djava.io.tmpdir=${TEMP_DIR} \
    -jar ${JAR_FILE} \
    --spring.config.location=${BASE_DIR}/config/application.yaml \
    --logging.config=file://${BASE_DIR}/config/logback.xml \
    --registerFilePath=${BASE_DIR}/registerCode.key \
    --isRegister=1
)

